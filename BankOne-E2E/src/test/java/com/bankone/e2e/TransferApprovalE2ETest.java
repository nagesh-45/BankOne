package com.bankone.e2e;

import com.bankone.e2e.pages.LoginPage;
import com.bankone.e2e.support.TestConfig;
import com.bankone.e2e.support.WebDriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Staff transfer-approvals screen loads after login.
 * Pending approve/reject is covered by TransferApprovalApiTest (portal threshold path).
 */
class TransferApprovalE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = WebDriverFactory.create();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void transferApprovalsPageLoads() {
        new LoginPage(driver)
                .open(TestConfig.baseUrl())
                .login(TestConfig.username(), TestConfig.password());

        String base = TestConfig.baseUrl().endsWith("/")
                ? TestConfig.baseUrl().substring(0, TestConfig.baseUrl().length() - 1)
                : TestConfig.baseUrl();
        driver.get(base + "/app/transfer-approvals");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        assertTrue(driver.findElement(By.cssSelector("h1")).getText().toLowerCase().contains("transfer"));
        assertTrue(driver.getPageSource().contains("Pending queue")
                || driver.getPageSource().contains("pending"));
    }
}
