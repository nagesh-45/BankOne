package com.bankone.e2e.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin write-API client for money movements. Account autocomplete in the UI
 * reads the replica ({@code @Transactional(readOnly=true)}), so Selenium cannot
 * pick newly created accounts until after sync — which would defeat the lag assert.
 */
public final class BankOneApi {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final String apiBase;

    public BankOneApi() {
        this(TestConfig.apiBaseUrl());
    }

    public BankOneApi(String apiBase) {
        this.apiBase = trimSlash(apiBase);
    }

    public String login(String username, String password) {
        String body = "{\"username\":\"" + escape(username)
                + "\",\"password\":\"" + escape(password) + "\"}";
        HttpResponse<String> res = postJson("/auth/login", null, body);
        if (res.statusCode() != 200) {
            throw new IllegalStateException("Login failed HTTP " + res.statusCode() + ": " + res.body());
        }
        return extractJsonString(res.body(), "accessToken");
    }

    public void deposit(String token, long accountId, String amount) {
        HttpResponse<String> res = postJson(
                "/accounts/" + accountId + "/deposit",
                token,
                "{\"amount\":" + amount + "}"
        );
        requireOk(res, "deposit");
    }

    public void withdraw(String token, long accountId, String amount) {
        HttpResponse<String> res = postJson(
                "/accounts/" + accountId + "/withdraw",
                token,
                "{\"amount\":" + amount + "}"
        );
        requireOk(res, "withdraw");
    }

    public void transfer(String token, long fromAccountId, long toAccountId, String amount) {
        HttpResponse<String> res = postJson(
                "/accounts/" + fromAccountId + "/transfer",
                token,
                "{\"toAccountId\":" + toAccountId + ",\"amount\":" + amount + "}"
        );
        requireOk(res, "transfer");
    }

    private HttpResponse<String> postJson(String path, String bearer, String json) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(apiBase + path))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            if (bearer != null && !bearer.isBlank()) {
                b.header("Authorization", "Bearer " + bearer);
            }
            return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new IllegalStateException("HTTP POST " + path + " failed: " + ex.getMessage(), ex);
        }
    }

    private static void requireOk(HttpResponse<String> res, String op) {
        int code = res.statusCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException(op + " failed HTTP " + code + ": " + res.body());
        }
    }

    private static String extractJsonString(String json, String field) {
        String needle = "\"" + field + "\"";
        int i = json.indexOf(needle);
        if (i < 0) {
            throw new IllegalStateException("Missing " + field + " in: " + json);
        }
        int colon = json.indexOf(':', i + needle.length());
        int startQuote = json.indexOf('"', colon + 1);
        int endQuote = json.indexOf('"', startQuote + 1);
        if (startQuote < 0 || endQuote < 0) {
            throw new IllegalStateException("Bad JSON for " + field + ": " + json);
        }
        return json.substring(startQuote + 1, endQuote);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String trimSlash(String base) {
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
