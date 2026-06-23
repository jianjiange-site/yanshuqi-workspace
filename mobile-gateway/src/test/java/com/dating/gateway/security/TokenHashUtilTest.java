package com.dating.gateway.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TokenHashUtilTest {

    @Test
    void sha256Hex_shouldBeDeterministicAndOnlyStoreHash() {
        String plain = "refresh-token-plain-text";
        String hash1 = TokenHashUtil.sha256Hex(plain);
        String hash2 = TokenHashUtil.sha256Hex(plain);
        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length());
        assertNotEquals(plain, hash1);
    }
}
