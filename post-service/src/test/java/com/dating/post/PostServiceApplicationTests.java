package com.dating.post;

import com.dating.post.health.MinioInfraHealthChecker;
import com.dating.post.health.NacosInfraHealthChecker;
import com.dating.post.health.PostgresInfraHealthChecker;
import com.dating.post.health.RedisInfraHealthChecker;
import com.dating.post.manager.PostCommentManager;
import com.dating.post.manager.PostImageManager;
import com.dating.post.manager.PostLikeManager;
import com.dating.post.manager.PostManager;
import com.dating.post.manager.PostStatManager;
import com.dating.post.mapper.PostCommentMapper;
import com.dating.post.mapper.PostImageMapper;
import com.dating.post.mapper.PostLikeMapper;
import com.dating.post.mapper.PostMapper;
import com.dating.post.mapper.PostStatMapper;
import com.dating.post.client.UserProfileClient;
import com.dating.post.repository.FeedPoolRepository;
import com.dating.post.repository.PostStatDeltaRepository;
import com.dating.post.repository.ReadHistoryRepository;
import com.dating.post.repository.UserTimelineRepository;
import com.dating.post.service.FeedPoolRebuildService;
import com.dating.post.service.FeedScoreService;
import com.dating.post.service.FeedService;
import com.dating.post.service.PostCacheService;
import com.dating.post.service.PostFanoutService;
import com.dating.post.service.ReadHistoryService;
import com.dating.post.service.PostCommentService;
import com.dating.post.service.PostLikeService;
import com.dating.post.service.PostReadService;
import com.dating.post.service.PostStatFlushService;
import com.dating.post.service.PostStatReadService;
import com.dating.post.service.PostWriteService;
import com.dating.post.service.support.BusinessIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PostServiceApplicationTests {

    @MockBean
    private PostgresInfraHealthChecker postgresInfraHealthChecker;

    @MockBean
    private RedisInfraHealthChecker redisInfraHealthChecker;

    @MockBean
    private NacosInfraHealthChecker nacosInfraHealthChecker;

    @MockBean
    private MinioInfraHealthChecker minioInfraHealthChecker;

    @MockBean
    private PostMapper postMapper;

    @MockBean
    private PostImageMapper postImageMapper;

    @MockBean
    private PostStatMapper postStatMapper;

    @MockBean
    private PostLikeMapper postLikeMapper;

    @MockBean
    private PostCommentMapper postCommentMapper;

    @MockBean
    private PostManager postManager;

    @MockBean
    private PostImageManager postImageManager;

    @MockBean
    private PostStatManager postStatManager;

    @MockBean
    private PostLikeManager postLikeManager;

    @MockBean
    private PostCommentManager postCommentManager;

    @MockBean
    private PostStatDeltaRepository postStatDeltaRepository;

    @MockBean
    private FeedPoolRepository feedPoolRepository;

    @MockBean
    private UserTimelineRepository userTimelineRepository;

    @MockBean
    private ReadHistoryRepository readHistoryRepository;

    @MockBean
    private UserProfileClient userProfileClient;

    @MockBean
    private PostCacheService postCacheService;

    @MockBean
    private PostStatReadService postStatReadService;

    @MockBean
    private PostStatFlushService postStatFlushService;

    @MockBean
    private PostLikeService postLikeService;

    @MockBean
    private PostCommentService postCommentService;

    @MockBean
    private FeedScoreService feedScoreService;

    @MockBean
    private FeedPoolRebuildService feedPoolRebuildService;

    @MockBean
    private FeedService feedService;

    @MockBean
    private ReadHistoryService readHistoryService;

    @MockBean
    private PostFanoutService postFanoutService;

    @MockBean
    private PostWriteService postWriteService;

    @MockBean
    private PostReadService postReadService;

    @MockBean
    private BusinessIdGenerator businessIdGenerator;

    @Test
    void contextLoads() {
    }
}
