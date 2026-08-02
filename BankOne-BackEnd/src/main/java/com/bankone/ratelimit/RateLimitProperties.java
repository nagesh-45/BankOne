package com.bankone.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds app.rate-limit.* from application.properties.
 * Learning: central place for limits so you don't hardcode 10/120 in the filter.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /** Master switch for the HTTP filter. */
    private boolean enabled = true;

    /** When true, buckets are stored in Redis (local learning). */
    private boolean redisEnabled = true;

    /** Login attempts allowed per client key per minute. */
    private int loginPerMinute = 10;

    /** General API calls allowed per client key per minute. */
    private int apiPerMinute = 120;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }

    public int getLoginPerMinute() {
        return loginPerMinute;
    }

    public void setLoginPerMinute(int loginPerMinute) {
        this.loginPerMinute = loginPerMinute;
    }

    public int getApiPerMinute() {
        return apiPerMinute;
    }

    public void setApiPerMinute(int apiPerMinute) {
        this.apiPerMinute = apiPerMinute;
    }
}