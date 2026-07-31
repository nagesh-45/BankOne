package com.bankone.e2e;

import com.bankone.e2e.pages.CustomerCreateDialog;
import com.bankone.e2e.pages.LoginPage;
import com.bankone.e2e.pages.ManagementPage;
import com.bankone.e2e.support.TestConfig;
import com.bankone.e2e.support.WebDriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalTransferE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = WebDriverFactory.create();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void createPortalCustomerAndOpenPortalAccounts() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String email = "e2e.portal." + suffix + "@bankone.test";
        String phone = "9" + String.format("%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));
        String portalUser = "pu" + suffix.substring(suffix.length() - 6);
        String portalPass = "Portal@123";

        new LoginPage(driver)
                .open(TestConfig.baseUrl())
                .login(TestConfig.username(), TestConfig.password());

        CustomerCreateDialog dialog = new ManagementPage(driver)
                .open(TestConfig.baseUrl())
                .openCreateCustomer();

        dialog.fill("Portal", "E2E" + suffix.substring(suffix.length() - 4), email, phone, "9 Portal St");

        WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='create-portal-login-checkbox']")));
        WebElement input = checkbox.findElement(By.cssSelector("input[type='checkbox']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);

        WebElement portalUsername = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("portalUsername")));
        portalUsername.clear();
        portalUsername.sendKeys(portalUser);
        WebElement portalPassword = driver.findElement(By.name("portalPassword"));
        portalPassword.clear();
        portalPassword.sendKeys(portalPass);
        dialog.submitAndWaitForSuccess();

        String base = TestConfig.baseUrl().endsWith("/")
                ? TestConfig.baseUrl().substring(0, TestConfig.baseUrl().length() - 1)
                : TestConfig.baseUrl();
        driver.manage().deleteAllCookies();
        driver.get(base + "/");
        new LoginPage(driver).open(base).login(portalUser, portalPass);

        wait.until(ExpectedConditions.urlContains("/portal"));
        assertTrue(driver.getCurrentUrl().contains("/portal"));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='portal-transfer-btn']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='portal-transfer-submit']")));
        assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Transfer"));
    }
}
