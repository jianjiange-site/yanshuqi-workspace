package com.dating.gateway.config;

import com.dating.gateway.security.JwtAuthFilter;
import com.dating.gateway.security.JwtProperties;
import com.dating.gateway.security.JwtVerifier;
import com.dating.gateway.security.SmsProperties;
import com.dating.gateway.security.TokenBlacklistService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * 鉴权相关 Bean 注册。
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, SmsProperties.class})
public class AuthConfiguration {

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtVerifier jwtVerifier,
                                       TokenBlacklistService tokenBlacklistService,
                                       Environment environment) {
        return new JwtAuthFilter(jwtVerifier, tokenBlacklistService, environment);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthFilter);
        registration.addUrlPatterns("/api/v1/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.setName("jwtAuthFilter");
        return registration;
    }
}
