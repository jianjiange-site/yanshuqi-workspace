package com.dating.match.manager;

import com.dating.match.entity.UserSwipeHistoryEntity;
import com.dating.match.mapper.UserSwipeHistoryMapper;
import com.dating.match.service.support.BusinessIdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class SwipeHistoryManagerUnitTest {

    @Mock
    private UserSwipeHistoryMapper userSwipeHistoryMapper;

    @Mock
    private BusinessIdGenerator businessIdGenerator;

    @InjectMocks
    private SwipeHistoryManager swipeHistoryManager;

    @Test
    void insertIfAbsent_whenDuplicateKey_shouldReturnExisting() {
        when(businessIdGenerator.nextId()).thenReturn(90001L);
        when(userSwipeHistoryMapper.insert(any(UserSwipeHistoryEntity.class))).thenThrow(new DuplicateKeyException("duplicate"));

        UserSwipeHistoryEntity existing = new UserSwipeHistoryEntity();
        existing.setId(1L);
        existing.setBizId(80001L);
        existing.setUserId(1001L);
        existing.setTargetUserId(2002L);
        when(userSwipeHistoryMapper.selectOne(any())).thenReturn(null).thenReturn(existing);

        UserSwipeHistoryEntity result = swipeHistoryManager.insertIfAbsent(1001L, 2002L, 1, 2, null);

        assertEquals(existing.getBizId(), result.getBizId());
        verify(userSwipeHistoryMapper, times(1)).insert(any(UserSwipeHistoryEntity.class));
    }
}
