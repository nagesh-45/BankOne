package com.bankone.audit.domain;

/** Stable action codes stored on each audit event. */
public final class AuditAction {

    private AuditAction() {
    }

    // AUTH
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";

    // CUSTOMER
    public static final String CUSTOMER_CREATE = "CUSTOMER_CREATE";
    public static final String CUSTOMER_UPDATE = "CUSTOMER_UPDATE";

    // ACCOUNT
    public static final String ACCOUNT_OPEN = "ACCOUNT_OPEN";
    public static final String ACCOUNT_DEPOSIT = "ACCOUNT_DEPOSIT";
    public static final String ACCOUNT_WITHDRAW = "ACCOUNT_WITHDRAW";
    public static final String ACCOUNT_TRANSFER = "ACCOUNT_TRANSFER";
    public static final String ACCOUNT_STATUS_CHANGE = "ACCOUNT_STATUS_CHANGE";

    // TRANSFER / approvals
    public static final String TRANSFER_REQUESTED = "TRANSFER_REQUESTED";
    public static final String TRANSFER_EXECUTED = "TRANSFER_EXECUTED";
    public static final String TRANSFER_APPROVED = "TRANSFER_APPROVED";
    public static final String TRANSFER_REJECTED = "TRANSFER_REJECTED";

    // STAFF users
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";

    // ROLE
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";

    // POLICY
    public static final String POLICY_UPDATE = "POLICY_UPDATE";

    // PORTAL
    public static final String BENEFICIARY_CREATE = "BENEFICIARY_CREATE";
    public static final String BENEFICIARY_DELETE = "BENEFICIARY_DELETE";
    public static final String PORTAL_TRANSFER = "PORTAL_TRANSFER";
}
