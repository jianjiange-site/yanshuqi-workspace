package com.dating.match.recommend;

import com.dating.match.constant.UserTypeConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedMergeServiceTest {

  private FeedMergeService feedMergeService;

  @BeforeEach
  void setUp() {
    feedMergeService = new FeedMergeService();
  }

  @Test
  void merge_shouldTakeBhByRatioWhenEnough() {
    List<CandidateProfile> bh = profiles(UserTypeConstant.BH, 20001L, 100);
    List<CandidateProfile> dh = profiles(UserTypeConstant.DH, 30001L, 300);
    List<FeedQueueItem> merged = feedMergeService.merge(bh, dh, 240, 0.20);
    long bhCount = merged.stream().filter(item -> item.getTargetUserType() == UserTypeConstant.BH).count();
    assertEquals(48, bhCount);
    assertEquals(240, merged.size());
  }

  @Test
  void merge_shouldFillWithDhWhenBhInsufficient() {
    List<CandidateProfile> bh = profiles(UserTypeConstant.BH, 20001L, 10);
    List<CandidateProfile> dh = profiles(UserTypeConstant.DH, 30001L, 240);
    List<FeedQueueItem> merged = feedMergeService.merge(bh, dh, 240, 0.20);
    assertEquals(10, merged.stream().filter(item -> item.getTargetUserType() == UserTypeConstant.BH).count());
    assertEquals(240, merged.size());
  }

  @Test
  void merge_shouldReturnAvailableWhenDhInsufficient() {
    List<CandidateProfile> bh = profiles(UserTypeConstant.BH, 20001L, 5);
    List<CandidateProfile> dh = profiles(UserTypeConstant.DH, 30001L, 10);
    List<FeedQueueItem> merged = feedMergeService.merge(bh, dh, 240, 0.20);
    assertEquals(15, merged.size());
  }

  @Test
  void merge_shouldCapAtQueueSize() {
    List<CandidateProfile> bh = profiles(UserTypeConstant.BH, 20001L, 300);
    List<CandidateProfile> dh = profiles(UserTypeConstant.DH, 30001L, 300);
    assertEquals(240, feedMergeService.merge(bh, dh, 240, 0.20).size());
  }

  @Test
  void merge_shouldInterleaveBhAndDh() {
    List<CandidateProfile> bh = profiles(UserTypeConstant.BH, 20001L, 2);
    List<CandidateProfile> dh = profiles(UserTypeConstant.DH, 30001L, 2);
    List<FeedQueueItem> merged = feedMergeService.merge(bh, dh, 4, 0.50);
    assertEquals(UserTypeConstant.BH, merged.get(0).getTargetUserType());
    assertEquals(UserTypeConstant.DH, merged.get(1).getTargetUserType());
    assertTrue(merged.size() >= 2);
  }

  private static List<CandidateProfile> profiles(int userType, long baseId, int count) {
    List<CandidateProfile> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      CandidateProfile profile = new CandidateProfile();
      profile.setUserId(baseId + i);
      profile.setUserType(userType);
      list.add(profile);
    }
    return list;
  }
}
