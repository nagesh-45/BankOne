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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountMoneyE2ETest {

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
    void createCustomerThenDepositAndWithdraw() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String email = "e2e.money." + suffix + "@bankone.test";
        String phone = "9" + String.format("%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));
        String lastName = "Money" + suffix.substring(suffix.length() - 4);

        new LoginPage(driver)
                .open(TestConfig.baseUrl())
                .login(TestConfig.username(), TestConfig.password());

        CustomerCreateDialog dialog = new ManagementPage(driver)
                .open(TestConfig.baseUrl())
                .openCreateCustomer();
        dialog.fill("E2E", lastName, email, phone, "1 Money St")
                .submitAndWaitForSuccess();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='customer-created-view']"))).click();

        driver.get(trimSlash(TestConfig.baseUrl()) + "/app/accounts");
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='accounts-deposit-btn']"))).click();

        pickAccountAndAmount(lastName, "250");
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='deposit-submit']"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("[data-testid='deposit-submit']")));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='accounts-withdraw-btn']"))).click();
        pickAccountAndAmount(lastName, "50");
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='withdraw-submit']"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("[data-testid='withdraw-submit']")));

        assertTrue(driver.getCurrentUrl().contains("/app/accounts"));
    }

    private void pickAccountAndAmount(String search, String amountValue) {
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("accountSearch")));
        searchBox.clear();
        searchBox.sendKeys(search);
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("mat-option"), 0));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("mat-option:not([disabled])"))).click();
        WebElement amount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("amount")));
        amount.clear();
        amount.sendKeys(amountValue);
    }

    private static String trimSlash(String base) {
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
