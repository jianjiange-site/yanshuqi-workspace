package com.dating.match.service;

import com.dating.match.dto.SuperHiResult;

/**
 * SuperHi 幂等结果编解码。
 */
final class SuperHiIdempotencyCodec {

    private SuperHiIdempotencyCodec() {
    }

    static String format(SuperHiResult result) {
        return "matchId=" + result.getMatchId() + ",coinsUsed=" + result.getCoinsUsed();
    }

    static SuperHiResult parse(String value) {
        long matchId = 0L;
        int coinsUsed = 0;
        for (String part : value.split(",")) {
            String[] kv = part.split("=");
            if (kv.length != 2) {
                continue;
            }
            if ("matchId".equals(kv[0])) {
                matchId = Long.parseLong(kv[1]);
            } else if ("coinsUsed".equals(kv[0])) {
                coinsUsed = Integer.parseInt(kv[1]);
            }
        }
        return new SuperHiResult(matchId, coinsUsed);
    }
}
