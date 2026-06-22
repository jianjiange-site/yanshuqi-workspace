package com.dating.post.service;

import com.dating.post.client.UserProfileClient;
import com.dating.post.constant.GenderBucket;
import com.dating.post.dto.FeedCandidate;
import com.dating.post.dto.FeedCursor;
import com.dating.post.dto.FeedResult;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import com.dating.post.repository.FeedPoolRepository;
import com.dating.post.repository.UserTimelineRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Feed 推荐读取与三路混排。
 * <p>
 * 三路来源业务意义：
 * <ul>
 *   <li>recommend 热门池：近 3 天高互动帖，保证 Feed 质量</li>
 *   <li>timeline 好友时间线：社交关系内容，提升熟人曝光</li>
 *   <li>cold_start 冷启动池：新帖即时曝光，避免零互动帖永不被看见</li>
 * </ul>
 * 混排策略：第 3 位插好友帖、第 6 位插冷启动帖，其余位优先热门；某路为空自动降级到其他路。
 */
@Service
public class FeedService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;
    private static final int FETCH_BUFFER_MULTIPLIER = 4;

    private final UserProfileClient userProfileClient;
    private final FeedPoolRepository feedPoolRepository;
    private final UserTimelineRepository userTimelineRepository;
    private final ReadHistoryService readHistoryService;
    private final PostReadService postReadService;

    public FeedService(UserProfileClient userProfileClient,
                       FeedPoolRepository feedPoolRepository,
                       UserTimelineRepository userTimelineRepository,
                       ReadHistoryService readHistoryService,
                       PostReadService postReadService) {
        this.userProfileClient = userProfileClient;
        this.feedPoolRepository = feedPoolRepository;
        this.userTimelineRepository = userTimelineRepository;
        this.readHistoryService = readHistoryService;
        this.postReadService = postReadService;
    }

    public FeedResult getRecommendFeed(long callerUserId, int pageSize, String cursor) {
        if (callerUserId <= 0L) {
            throw new PostBusinessException(PostErrorCode.UNAUTHORIZED);
        }
        int resolvedPageSize = normalizePageSize(pageSize);
        FeedCursor feedCursor = FeedCursor.parse(cursor);

        GenderBucket callerGender = userProfileClient.getGenderBucket(callerUserId);
        GenderBucket targetPool = callerGender.opposite();

        int fetchSize = resolvedPageSize * FETCH_BUFFER_MULTIPLIER;
        List<Long> recommendIds = readHistoryService.filterUnread(
                callerUserId, feedPoolRepository.listRecommendPool(targetPool, feedCursor.getRecOffset(), fetchSize));
        List<Long> timelineIds = readHistoryService.filterUnread(
                callerUserId, userTimelineRepository.listTimeline(callerUserId, fetchSize));
        List<Long> coldStartIds = readHistoryService.filterUnread(
                callerUserId, feedPoolRepository.listColdStartPool(targetPool, feedCursor.getCsOffset(), fetchSize));

        MixContext mixContext = mixCandidates(resolvedPageSize, recommendIds, timelineIds, coldStartIds);

        List<PostInfoDTO> items = new ArrayList<>();
        List<Long> readPostIds = new ArrayList<>();
        for (FeedCandidate candidate : mixContext.candidates()) {
            try {
                PostInfoDTO detail = postReadService.getPostDetail(candidate.getPostId(), callerUserId);
                items.add(detail);
                readPostIds.add(candidate.getPostId());
            } catch (PostBusinessException ex) {
                if (ex.getErrorCode() == PostErrorCode.POST_NOT_FOUND) {
                    continue;
                }
                throw ex;
            }
        }

        if (!readPostIds.isEmpty()) {
            readHistoryService.markRead(callerUserId, readPostIds);
        }

        FeedCursor nextCursor = feedCursor.advance(mixContext.nextRecOffset(), mixContext.nextCsOffset());
        boolean hasMore = items.size() >= resolvedPageSize
                && (mixContext.hasMoreCandidates()
                || feedPoolRepository.hasMoreRecommend(targetPool, nextCursor.getRecOffset())
                || feedPoolRepository.hasMoreColdStart(targetPool, nextCursor.getCsOffset())
                || !timelineIds.isEmpty());

        FeedResult result = new FeedResult();
        result.setItems(items);
        result.setNextCursor(hasMore ? nextCursor.encode() : "");
        result.setHasMore(hasMore);
        return result;
    }

    /**
     * 按槽位混排：1-based 第 3 位 timeline 优先，第 6 位 cold_start 优先，其余 recommend 优先。
     */
    MixContext mixCandidates(int pageSize,
                             List<Long> recommendIds,
                             List<Long> timelineIds,
                             List<Long> coldStartIds) {
        int recIdx = 0;
        int tlIdx = 0;
        int csIdx = 0;
        Set<Long> used = new HashSet<>();
        List<FeedCandidate> picked = new ArrayList<>();

        for (int slot = 1; slot <= pageSize; slot++) {
            FeedCandidate.Source[] priorities = prioritiesForSlot(slot);
            PickResult pickResult = pickNext(priorities, recommendIds, timelineIds, coldStartIds,
                    recIdx, tlIdx, csIdx, used);
            if (pickResult == null) {
                break;
            }
            picked.add(pickResult.candidate());
            recIdx = pickResult.recIdx();
            tlIdx = pickResult.tlIdx();
            csIdx = pickResult.csIdx();
        }

        boolean hasMoreCandidates = recIdx < recommendIds.size()
                || tlIdx < timelineIds.size()
                || csIdx < coldStartIds.size();
        return new MixContext(picked, recIdx, csIdx, hasMoreCandidates);
    }

    private FeedCandidate.Source[] prioritiesForSlot(int slot) {
        if (slot == 3) {
            return new FeedCandidate.Source[]{
                    FeedCandidate.Source.TIMELINE,
                    FeedCandidate.Source.RECOMMEND,
                    FeedCandidate.Source.COLD_START
            };
        }
        if (slot == 6) {
            return new FeedCandidate.Source[]{
                    FeedCandidate.Source.COLD_START,
                    FeedCandidate.Source.RECOMMEND,
                    FeedCandidate.Source.TIMELINE
            };
        }
        return new FeedCandidate.Source[]{
                FeedCandidate.Source.RECOMMEND,
                FeedCandidate.Source.TIMELINE,
                FeedCandidate.Source.COLD_START
        };
    }

    private PickResult pickNext(FeedCandidate.Source[] priorities,
                                List<Long> recommendIds,
                                List<Long> timelineIds,
                                List<Long> coldStartIds,
                                int recIdx,
                                int tlIdx,
                                int csIdx,
                                Set<Long> used) {
        int currentRecIdx = recIdx;
        int currentTlIdx = tlIdx;
        int currentCsIdx = csIdx;

        for (FeedCandidate.Source source : priorities) {
            List<Long> list = switch (source) {
                case RECOMMEND -> recommendIds;
                case TIMELINE -> timelineIds;
                case COLD_START -> coldStartIds;
            };
            int idx = switch (source) {
                case RECOMMEND -> currentRecIdx;
                case TIMELINE -> currentTlIdx;
                case COLD_START -> currentCsIdx;
            };
            for (int i = idx; i < list.size(); i++) {
                long postId = list.get(i);
                if (source == FeedCandidate.Source.RECOMMEND) {
                    currentRecIdx = i + 1;
                } else if (source == FeedCandidate.Source.TIMELINE) {
                    currentTlIdx = i + 1;
                } else {
                    currentCsIdx = i + 1;
                }
                if (used.add(postId)) {
                    return new PickResult(new FeedCandidate(postId, source),
                            currentRecIdx, currentTlIdx, currentCsIdx);
                }
            }
        }
        return null;
    }

    static int normalizePageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    record MixContext(List<FeedCandidate> candidates,
                      int nextRecOffset,
                      int nextCsOffset,
                      boolean hasMoreCandidates) {
    }

    private record PickResult(FeedCandidate candidate, int recIdx, int tlIdx, int csIdx) {
    }
}
