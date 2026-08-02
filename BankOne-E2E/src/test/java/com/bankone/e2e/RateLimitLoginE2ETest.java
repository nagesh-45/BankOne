package com.bankone.e2e;

import com.bankone.e2e.pages.LoginPage;
import com.bankone.e2e.support.TestConfig;
import com.bankone.e2e.support.WebDriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Selenium E2E for Redis login rate limiting.
 *
 * Captures /auth/login HTTP statuses by patching XHR + fetch (no CDP — Chrome 150 mismatch).
 *
 * Run:
 *   export BANKONE_RATE_LIMIT_E2E=true
 *   cd BankOne-E2E && mvn -B -Dtest=RateLimitLoginE2ETest test
 */
@EnabledIfEnvironmentVariable(named = "BANKONE_RATE_LIMIT_E2E", matches = "true")
class RateLimitLoginE2ETest {

    private static final String FAKE_USER = "rl_e2e_nouser";
    private static final int ATTEMPTS = 15;
    private static final int LOGIN_LIMIT = 10;

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        assumeTrue(TestConfig.baseUrl().contains("localhost")
                        || TestConfig.baseUrl().contains("127.0.0.1"),
                "Rate-limit E2E expects local UI");
        driver = WebDriverFactory.create();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Repeated failed logins eventually get HTTP 429 from rate limiter")
    void repeatedFailedLoginsEventuallyReturn429() {
        LoginPage loginPage = new LoginPage(driver).open(TestConfig.baseUrl());
        installLoginStatusProbe();

        for (int i = 1; i <= ATTEMPTS; i++) {
            loginPage.attemptFailedLogin(
                    FAKE_USER,
                    "wrong-password-" + i,
                    () -> readCapturedLoginStatuses().size()
            );
            assertTrue(loginPage.isStillOnLoginPage(),
                    "Attempt " + i + " must stay on login page");
        }

        List<Integer> statuses = readCapturedLoginStatuses();
        assertFalse(statuses.isEmpty(),
                "JS probe should have captured /auth/login statuses. Got: " + statuses);

        assertTrue(
                statuses.stream().anyMatch(s -> s == 429),
                "Expected at least one HTTP 429 after ~" + LOGIN_LIMIT
                        + " attempts. Captured statuses: " + statuses
        );

        long earlyAuthFailures = statuses.stream()
                .limit(Math.min(LOGIN_LIMIT, statuses.size()))
                .filter(s -> s == 401 || s == 403 || s == 400)
                .count();
        assertTrue(earlyAuthFailures >= 1,
                "Expected some 401s before 429. Captured: " + statuses);

        boolean sawRateLimitMessage = loginPage.snackbarVisibleWithText("too many");
        if (!sawRateLimitMessage) {
            System.out.println("Note: snackbar may have timed out; "
                    + "429 confirmed via network probe. Statuses=" + statuses);
        }
    }

    private void installLoginStatusProbe() {
        ((JavascriptExecutor) driver).executeScript(
                """
                (function () {
                  window.__bankoneLoginStatuses = window.__bankoneLoginStatuses || [];
                  if (window.__bankoneLoginProbeInstalled) {
                    return;
                  }
                  window.__bankoneLoginProbeInstalled = true;

                  function record(url, status) {
                    if (String(url || '').indexOf('/auth/login') !== -1) {
                      window.__bankoneLoginStatuses.push(status);
                    }
                  }

                  var open = XMLHttpRequest.prototype.open;
                  var send = XMLHttpRequest.prototype.send;
                  XMLHttpRequest.prototype.open = function (method, url) {
                    this.__bankoneUrl = url;
                    return open.apply(this, arguments);
                  };
                  XMLHttpRequest.prototype.send = function () {
                    var xhr = this;
                    xhr.addEventListener('loadend', function () {
                      record(xhr.__bankoneUrl, xhr.status);
                    });
                    return send.apply(this, arguments);
                  };

                  if (window.fetch) {
                    var origFetch = window.fetch.bind(window);
                    window.fetch = function (input, init) {
                      var url = (typeof input === 'string')
                        ? input
                        : (input && input.url ? input.url : String(input));
                      return origFetch(input, init).then(function (res) {
                        record(url, res.status);
                        return res;
                      });
                    };
                  }
                })();
                """
        );
    }

    private List<Integer> readCapturedLoginStatuses() {
        Object raw = ((JavascriptExecutor) driver).executeScript(
                "return window.__bankoneLoginStatuses || [];"
        );
        List<Integer> out = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Number n) {
                    out.add(n.intValue());
                }
            }
        }
        return out;
    }
}
