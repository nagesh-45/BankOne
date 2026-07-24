package com.bankone.notification.dto;

public class BankActionEvent {

    private String action;
    private String entityType;
    private String entityId;
    private String summary;
    private String actor;
    private String occurredAt;
        /** Customer email from onboarding — primary notification recipient. */
    private String recipientEmail;

    public BankActionEvent() {
    }

    public BankActionEvent(
            String action,
            String entityType,
            String entityId,
            String summary,
            String actor,
            String occurredAt,
            String recipientEmail) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.summary = summary;
        this.actor = actor;
        this.occurredAt = occurredAt;
        this.recipientEmail = recipientEmail;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
}
