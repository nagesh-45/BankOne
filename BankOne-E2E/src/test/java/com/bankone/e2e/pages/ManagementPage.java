package com.bankone.e2e.pages;

import com.bankone.e2e.support.DomHelpers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ManagementPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By CREATE_CUSTOMER_BTN = By.cssSelector("[data-testid='create-customer-btn']");
    private static final By CREATE_EMPLOYEE_BTN = By.cssSelector("[data-testid='create-employee-btn']");
    private static final By REPLICA_SYNC_BTN = By.cssSelector("[data-testid='replica-sync-btn']");

    public ManagementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public ManagementPage open(String baseUrl) {
        driver.get(trimTrailingSlash(baseUrl) + "/app/management");
        DomHelpers.dismissViteErrorOverlay(driver);
        wait.until(ExpectedConditions.elementToBeClickable(CREATE_CUSTOMER_BTN));
        return this;
    }

    public CustomerCreateDialog openCreateCustomer() {
        DomHelpers.dismissViteErrorOverlay(driver);
        wait.until(ExpectedConditions.elementToBeClickable(CREATE_CUSTOMER_BTN)).click();
        return new CustomerCreateDialog(driver);
    }

    public EmployeeCreateDialog openCreateEmployee() {
        DomHelpers.dismissViteErrorOverlay(driver);
        wait.until(ExpectedConditions.elementToBeClickable(CREATE_EMPLOYEE_BTN)).click();
        return new EmployeeCreateDialog(driver);
    }

    public ManagementPage syncReplicaNow() {
        DomHelpers.dismissViteErrorOverlay(driver);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(REPLICA_SYNC_BTN));
        btn.click();
        // Button shows Syncing… then returns to enabled "Sync replica now"
        wait.until(d -> {
            WebElement b = d.findElement(REPLICA_SYNC_BTN);
            String text = b.getText() == null ? "" : b.getText().toLowerCase();
            return b.isEnabled() && text.contains("sync replica");
        });
        return this;
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
