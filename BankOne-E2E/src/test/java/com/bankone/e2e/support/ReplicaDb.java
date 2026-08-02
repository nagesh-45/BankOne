package com.bankone.e2e.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Direct JDBC checks against write DB vs read replica.
 */
public final class ReplicaDb {

    private ReplicaDb() {
    }

    public static boolean customerExistsOnWrite(String email) {
        return exists(TestConfig.writeJdbcUrl(),
                "SELECT 1 FROM customers WHERE lower(email) = lower(?)", email);
    }

    public static boolean customerExistsOnReplica(String email) {
        return exists(TestConfig.readJdbcUrl(),
                "SELECT 1 FROM customers WHERE lower(email) = lower(?)", email);
    }

    public static boolean userExistsOnWrite(String username) {
        return exists(TestConfig.writeJdbcUrl(),
                "SELECT 1 FROM users WHERE lower(username) = lower(?)", username);
    }

    public static boolean userExistsOnReplica(String username) {
        return exists(TestConfig.readJdbcUrl(),
                "SELECT 1 FROM users WHERE lower(username) = lower(?)", username);
    }

    /** Primary (write) account id for the customer's first account, or -1 if none. */
    public static long accountIdOnWriteForCustomerEmail(String email) {
        return scalarLong(TestConfig.writeJdbcUrl(), """
                SELECT a.account_id FROM account a
                JOIN customers c ON c.customer_id = a.customer_id
                WHERE lower(c.email) = lower(?)
                ORDER BY a.account_id
                LIMIT 1
                """, email);
    }

    public static long transactionCountOnWriteForCustomerEmail(String email) {
        return count(TestConfig.writeJdbcUrl(), """
                SELECT COUNT(*) FROM bank_transaction t
                JOIN account a ON a.account_id = t.account_id
                JOIN customers c ON c.customer_id = a.customer_id
                WHERE lower(c.email) = lower(?)
                """, email);
    }

    public static long transactionCountOnReplicaForCustomerEmail(String email) {
        return count(TestConfig.readJdbcUrl(), """
                SELECT COUNT(*) FROM bank_transaction t
                JOIN account a ON a.account_id = t.account_id
                JOIN customers c ON c.customer_id = a.customer_id
                WHERE lower(c.email) = lower(?)
                """, email);
    }

    private static boolean exists(String jdbcUrl, String sql, String param) {
        try (Connection c = connect(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("JDBC exists failed: " + ex.getMessage(), ex);
        }
    }

    private static long count(String jdbcUrl, String sql, String param) {
        return scalarLong(jdbcUrl, sql, param);
    }

    private static long scalarLong(String jdbcUrl, String sql, String param) {
        try (Connection c = connect(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return -1L;
                }
                return rs.getLong(1);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("JDBC scalar failed: " + ex.getMessage(), ex);
        }
    }

    private static Connection connect(String jdbcUrl) throws Exception {
        return DriverManager.getConnection(
                jdbcUrl, TestConfig.dbUsername(), TestConfig.dbPassword());
    }
}
