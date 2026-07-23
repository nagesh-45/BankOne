package com.bankone.common.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Makes Render / Heroku-style DATABASE_URL and postgres:// URLs work with Spring JDBC.
 * Also forces sslmode=require for *.render.com hosts.
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${SPRING_DATASOURCE_URL:${DATABASE_URL:${spring.datasource.url:}}}") String rawUrl,
            @Value("${SPRING_DATASOURCE_USERNAME:${spring.datasource.username:}}") String username,
            @Value("${SPRING_DATASOURCE_PASSWORD:${spring.datasource.password:}}") String password
    ) {
        if (!StringUtils.hasText(rawUrl)) {
            throw new IllegalStateException(
                    "Database URL is missing. Set SPRING_DATASOURCE_URL or DATABASE_URL.");
        }

        String jdbcUrl = toJdbcUrl(rawUrl.trim());
        String user = username;
        String pass = password;

        // If URL embeds credentials (postgres://user:pass@host/db), prefer those when env user blank
        ParsedPostgresUrl parsed = tryParsePostgresStyle(rawUrl.trim());
        if (parsed != null) {
            jdbcUrl = parsed.jdbcUrl();
            if (!StringUtils.hasText(user)) {
                user = parsed.username();
            }
            if (!StringUtils.hasText(pass)) {
                pass = parsed.password();
            }
        }

        jdbcUrl = ensureSslModeForRender(jdbcUrl);

        String safeHost = jdbcUrl.replaceAll("(?i)(password=)[^&]*", "$1***");
        log.info("Configuring DataSource jdbcUrl={}", safeHost);

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(5);
        ds.setConnectionTimeout(30_000);
        return ds;
    }

    private static String toJdbcUrl(String url) {
        if (url.startsWith("jdbc:")) {
            return url;
        }
        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            ParsedPostgresUrl parsed = tryParsePostgresStyle(url);
            if (parsed != null) {
                return parsed.jdbcUrl();
            }
        }
        return url;
    }

    private static String ensureSslModeForRender(String jdbcUrl) {
        String lower = jdbcUrl.toLowerCase();
        if (!lower.contains("render.com") && !lower.contains("dpg-")) {
            return jdbcUrl;
        }
        if (lower.contains("sslmode=")) {
            return jdbcUrl;
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
    }

    private static ParsedPostgresUrl tryParsePostgresStyle(String url) {
        try {
            String normalized = url;
            if (normalized.startsWith("jdbc:")) {
                return null;
            }
            if (normalized.startsWith("postgres://")) {
                normalized = "postgresql://" + normalized.substring("postgres://".length());
            }
            if (!normalized.startsWith("postgresql://")) {
                return null;
            }
            URI uri = new URI(normalized);
            String userInfo = uri.getUserInfo();
            String user = null;
            String pass = null;
            if (userInfo != null) {
                String[] parts = userInfo.split(":", 2);
                user = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                if (parts.length > 1) {
                    pass = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                }
            }
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath() != null ? uri.getPath() : "";
            String db = path.startsWith("/") ? path.substring(1) : path;
            String query = uri.getQuery();
            String jdbc = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            if (StringUtils.hasText(query)) {
                jdbc = jdbc + "?" + query;
            }
            return new ParsedPostgresUrl(jdbc, user, pass);
        } catch (URISyntaxException ex) {
            log.warn("Could not parse postgres URL style: {}", ex.getMessage());
            return null;
        }
    }

    private record ParsedPostgresUrl(String jdbcUrl, String username, String password) {}
}
