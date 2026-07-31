package com.bankone.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ManagementPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By CREATE_CUSTOMER_BTN = By.cssSelector("[data-testid='create-customer-btn']");

    public ManagementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public ManagementPage open(String baseUrl) {
        driver.get(trimTrailingSlash(baseUrl) + "/app/management");
        wait.until(ExpectedConditions.elementToBeClickable(CREATE_CUSTOMER_BTN));
        return this;
    }

    public CustomerCreateDialog openCreateCustomer() {
        wait.until(ExpectedConditions.elementToBeClickable(CREATE_CUSTOMER_BTN)).click();
        return new CustomerCreateDialog(driver);
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
