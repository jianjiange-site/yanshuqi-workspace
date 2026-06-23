package com.dating.gateway.resolver;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.security.CurrentUserContext;
import com.dating.gateway.security.JwtClaims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 正式链路：从 {@link CurrentUserContext} 读取 JWT 解析结果。
 */
@Component
public class JwtCallerUserResolver implements CallerUserResolver {

    @Override
    public long resolveCallerUserId(HttpServletRequest request) {
        JwtClaims claims = CurrentUserContext.get();
        if (claims == null || claims.getUserId() <= 0) {
            throw new GatewayBizException(GatewayErrorCode.TOKEN_INVALID);
        }
        return claims.getUserId();
    }
}
