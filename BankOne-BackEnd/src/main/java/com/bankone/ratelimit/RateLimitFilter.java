package com.bankone.ratelimit;

import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Runs early: check Redis bucket before controller work.
 * Only registered when RateLimiterService exists (redis-enabled=true).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@ConditionalOnBean(RateLimiterService.class)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties properties;

    public RateLimitFilter(RateLimiterService rateLimiterService, RateLimitProperties properties) {
        this.rateLimiterService = rateLimiterService;
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        // Skip CORS preflight and static noise
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        RateLimiterService.Policy policy = isLogin(request)
                ? RateLimiterService.Policy.LOGIN
                : RateLimiterService.Policy.API;

        String clientKey = clientKey(request, policy);
        ConsumptionProbe probe = rateLimiterService.tryConsume(policy, clientKey);

        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
        response.setHeader("Retry-After", String.valueOf(Math.max(waitSeconds, 1)));
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":\"Too many requests\",\"policy\":\"" + policy + "\"}"
        );
    }

    private boolean isLogin(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/auth/login");
    }

    private String clientKey(HttpServletRequest request, RateLimiterService.Policy policy) {
        // Login: always by IP (attacker may not have a username yet / may spray users)
        if (policy == RateLimiterService.Policy.LOGIN) {
            return clientIp(request);
        }
        // API: prefer authenticated username so each user has their own bucket
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getName() != null
                && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return clientIp(request);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }
}