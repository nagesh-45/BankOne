package com.bankone.audit.domain;

/**
 * High-level buckets for the activity audit trail.
 * AUTH is login / logout / password (kept separate from business events).
 */
public enum AuditCategory {
    AUTH,
    CUSTOMER,
    ACCOUNT,
    TRANSFER,
    STAFF,
    ROLE,
    POLICY,
    PORTAL
}
