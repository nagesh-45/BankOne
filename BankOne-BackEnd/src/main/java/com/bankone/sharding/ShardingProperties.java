package com.bankone.sharding;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.sharding")
public class ShardingProperties {

    private boolean enabled = true;
    private int shardCount = 2;
    private String username = "bankone_user";
    private String password = "BankOne@123";
    private String s0Url = "jdbc:postgresql://localhost:5432/bankone_s0";
    private String s1Url = "jdbc:postgresql://localhost:5432/bankone_s1";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getShardCount() {
        return shardCount;
    }

    public void setShardCount(int shardCount) {
        this.shardCount = shardCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getS0Url() {
        return s0Url;
    }

    public void setS0Url(String s0Url) {
        this.s0Url = s0Url;
    }

    public String getS1Url() {
        return s1Url;
    }

    public void setS1Url(String s1Url) {
        this.s1Url = s1Url;
    }
}
