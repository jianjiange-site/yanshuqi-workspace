package com.dating.match.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MatchManagerUnitTest {

    @Test
    void normalizePair_shouldSortLowHigh() {
        assertArrayEquals(new long[]{100L, 200L}, MatchManager.normalizePair(200L, 100L));
        assertArrayEquals(new long[]{100L, 200L}, MatchManager.normalizePair(100L, 200L));
    }
}
