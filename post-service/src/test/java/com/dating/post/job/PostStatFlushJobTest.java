package com.dating.post.job;

import com.dating.post.manager.PostStatManager;
import com.dating.post.repository.PostStatDeltaRepository;
import com.dating.post.service.PostStatFlushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostStatFlushJobTest {

    @Mock
    private PostStatDeltaRepository postStatDeltaRepository;

    @Mock
    private PostStatManager postStatManager;

    private PostStatFlushService postStatFlushService;

    @BeforeEach
    void setUp() {
        postStatFlushService = new PostStatFlushService(postStatDeltaRepository, postStatManager);
    }

    @Test
    void flushLikeDeltas_shouldApplyDbUpdate() {
        when(postStatDeltaRepository.listUpdatedPostIds()).thenReturn(Set.of(88001L));
        when(postStatDeltaRepository.atomicTakeLikeDelta(88001L)).thenReturn(3L);

        postStatFlushService.flushLikeDeltas();

        verify(postStatManager).addLikeCount(88001L, 3);
        verify(postStatDeltaRepository).removeUpdatedPostIfIdle(88001L);
    }

    @Test
    void flushCommentDeltas_shouldApplyDbUpdate() {
        when(postStatDeltaRepository.listUpdatedPostIds()).thenReturn(Set.of(88001L));
        when(postStatDeltaRepository.atomicTakeCommentDelta(88001L)).thenReturn(2L);

        postStatFlushService.flushCommentDeltas();

        verify(postStatManager).addCommentCount(88001L, 2);
        verify(postStatDeltaRepository).removeUpdatedPostIfIdle(88001L);
    }
}
