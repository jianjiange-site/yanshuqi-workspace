package com.dating.match.recommend;

import com.dating.match.constant.UserTypeConstant;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * D0 冷启动混排：按 bhRatio 取 BH，不足由 DH 补齐，尽量交错插入。
 */
@Service
public class FeedMergeService {

    public List<FeedQueueItem> merge(List<CandidateProfile> bhCandidates,
                                     List<CandidateProfile> dhCandidates,
                                     int queueSize,
                                     double bhRatio) {
        if (queueSize <= 0) {
            return List.of();
        }
        int targetBh = (int) Math.round(queueSize * bhRatio);
        int actualBh = Math.min(targetBh, bhCandidates == null ? 0 : bhCandidates.size());
        int actualDh = Math.min(queueSize - actualBh, dhCandidates == null ? 0 : dhCandidates.size());

        List<CandidateProfile> selectedBh = slice(bhCandidates, actualBh);
        List<CandidateProfile> selectedDh = slice(dhCandidates, actualDh);

        List<FeedQueueItem> merged = new ArrayList<>(actualBh + actualDh);
        int bi = 0;
        int di = 0;
        while (bi < selectedBh.size() || di < selectedDh.size()) {
            if (bi < selectedBh.size()) {
                merged.add(toItem(selectedBh.get(bi++)));
            }
            if (di < selectedDh.size()) {
                merged.add(toItem(selectedDh.get(di++)));
            }
            if (merged.size() >= queueSize) {
                break;
            }
        }
        if (merged.size() > queueSize) {
            return new ArrayList<>(merged.subList(0, queueSize));
        }
        return merged;
    }

    private static List<CandidateProfile> slice(List<CandidateProfile> candidates, int count) {
        if (candidates == null || count <= 0) {
            return List.of();
        }
        return candidates.subList(0, Math.min(count, candidates.size()));
    }

    private static FeedQueueItem toItem(CandidateProfile profile) {
        int userType = profile.getUserType() > 0 ? profile.getUserType() : UserTypeConstant.BH;
        return new FeedQueueItem(profile.getUserId(), userType);
    }
}
