package com.dating.gateway.client;

import com.dating.gateway.security.CurrentUserContext;
import com.dating.gateway.security.JwtClaims;
import io.grpc.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PostGrpcMetadataSupportTest {

    @AfterEach
    void tearDown() {
        CurrentUserContext.clear();
        MDC.clear();
    }

    @Test
    void buildMetadata_shouldIncludeCallerUserId() {
        Metadata metadata = PostGrpcMetadataSupport.buildMetadata(88001L);
        assertEquals("88001", metadata.get(PostGrpcMetadataSupport.USER_ID_KEY));
    }

    @Test
    void buildMetadata_shouldIncludeTraceAndDeviceWhenPresent() {
        MDC.put("traceId", "trace-gw4-001");
        CurrentUserContext.set(new JwtClaims(88001L, "jti", "device-gw4", 3, 1, Long.MAX_VALUE / 1000));

        Metadata metadata = PostGrpcMetadataSupport.buildMetadata(88001L);

        assertEquals("trace-gw4-001", metadata.get(PostGrpcMetadataSupport.TRACE_ID_KEY));
        assertEquals("device-gw4", metadata.get(PostGrpcMetadataSupport.DEVICE_ID_KEY));
    }

    @Test
    void buildMetadata_withoutOptionalFields_shouldOnlySetUserId() {
        Metadata metadata = PostGrpcMetadataSupport.buildMetadata(1L);
        assertNotNull(metadata.get(PostGrpcMetadataSupport.USER_ID_KEY));
        assertNull(metadata.get(PostGrpcMetadataSupport.TRACE_ID_KEY));
        assertNull(metadata.get(PostGrpcMetadataSupport.DEVICE_ID_KEY));
    }
}
