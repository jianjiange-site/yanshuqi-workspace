package com.dating.gateway.security;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.resolver.DevHeaderCallerUserResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 鉴权过滤器：保护 /api/v1/** 业务接口；Auth 登录/刷新接口放行。
 * <p>
 * {@code /callback/openim/**} 不在 /api/v1 下，由 {@link #shouldNotFilter} 整体跳过 JWT。
 * dev/test 在无 Bearer 时允许 {@code X-User-Id} 兜底，仅用于本地联调，prod 禁止。
 * Bean 由 {@link com.dating.gateway.config.AuthConfiguration} 注册，避免 WebMvcTest 切片误加载。
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATTERNS = List.of(
            "/api/v1/auth/send-sms-code",
            "/api/v1/auth/login-device",
            "/api/v1/auth/login-phone",
            "/api/v1/auth/login-third-party",
            "/api/v1/auth/refresh");

    private final JwtVerifier jwtVerifier;
    private final TokenBlacklistService tokenBlacklistService;
    private final Environment environment;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(JwtVerifier jwtVerifier,
                         TokenBlacklistService tokenBlacklistService,
                         Environment environment) {
        this.jwtVerifier = jwtVerifier;
        this.tokenBlacklistService = tokenBlacklistService;
        this.environment = environment;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            if (authenticateBearerToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (allowDevHeaderFallback(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            writeUnauthorized(response, GatewayErrorCode.TOKEN_INVALID.getMessage());
        } catch (GatewayBizException ex) {
            writeUnauthorized(response, ex.getDetailMessage());
        } finally {
            CurrentUserContext.clear();
        }
    }

    private boolean isPublicPath(String path) {
        for (String pattern : PUBLIC_PATTERNS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private boolean authenticateBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return false;
        }
        String token = authorization.substring("Bearer ".length()).trim();
        if (!StringUtils.hasText(token)) {
            return false;
        }
        JwtClaims claims = jwtVerifier.verifyAccessToken(token);
        if (tokenBlacklistService.isBlacklisted(claims.getJti())) {
            throw new GatewayBizException(GatewayErrorCode.TOKEN_REVOKED);
        }
        CurrentUserContext.set(claims);
        return true;
    }

    /**
     * dev/test 专用：无 JWT 时读取 X-User-Id 构造临时上下文，禁止用于 prod。
     */
    private boolean allowDevHeaderFallback(HttpServletRequest request) {
        if (!isDevOrTestProfile()) {
            return false;
        }
        String headerValue = request.getHeader(DevHeaderCallerUserResolver.HEADER_USER_ID);
        if (!StringUtils.hasText(headerValue)) {
            return false;
        }
        try {
            long userId = Long.parseLong(headerValue.trim());
            if (userId <= 0) {
                return false;
            }
            JwtClaims devClaims = new JwtClaims(
                    userId,
                    "dev-header",
                    "dev-device",
                    3,
                    null,
                    Long.MAX_VALUE / 1000);
            CurrentUserContext.set(devClaims);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isDevOrTestProfile() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return true;
        }
        return Arrays.stream(profiles).anyMatch(p -> "dev".equals(p) || "test".equals(p));
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":10501,\"message\":\"" + message + "\",\"data\":null}");
    }
}
