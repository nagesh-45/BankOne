package com.bankone.replica;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.replica")
public class ReplicaProperties {

    private boolean enabled = false;
    private String readUrl = "jdbc:postgresql://localhost:5432/bankone_read";
    private long syncMs = 120_000L;
    private String username = "bankone_user";
    private String password = "BankOne@123";
    /** Full audit_event copy is heavy locally — off by default. */
    private boolean includeAudit = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getReadUrl() {
        return readUrl;
    }

    public void setReadUrl(String readUrl) {
        this.readUrl = readUrl;
    }

    public long getSyncMs() {
        return syncMs;
    }

    public void setSyncMs(long syncMs) {
        this.syncMs = syncMs;
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

    public boolean isIncludeAudit() {
        return includeAudit;
    }

    public void setIncludeAudit(boolean includeAudit) {
        this.includeAudit = includeAudit;
    }
}
