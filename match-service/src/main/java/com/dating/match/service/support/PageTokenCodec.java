package com.dating.match.service.support;

import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * 列表分页 token 编解码：{@code epochMillis:bizId}。
 */
public final class PageTokenCodec {

    private PageTokenCodec() {
    }

    public static String encode(OffsetDateTime time, long bizId) {
        if (time == null) {
            return "";
        }
        return time.toInstant().toEpochMilli() + ":" + bizId;
    }

    public static Cursor decode(String pageToken) {
        if (!StringUtils.hasText(pageToken)) {
            return null;
        }
        String[] parts = pageToken.split(":");
        if (parts.length != 2) {
            return null;
        }
        OffsetDateTime time = OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(Long.parseLong(parts[0])), ZoneOffset.UTC);
        return new Cursor(time, Long.parseLong(parts[1]));
    }

    public record Cursor(OffsetDateTime time, long bizId) {
    }
}
