package com.bankone.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CustomerCreateDialog {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By FIRST_NAME = By.name("firstName");
    private static final By LAST_NAME = By.name("lastName");
    private static final By EMAIL = By.name("email");
    private static final By PHONE = By.name("phoneNumber");
    private static final By ADDRESS = By.name("address");
    private static final By SUBMIT = By.cssSelector("[data-testid='customer-create-submit']");
    private static final By CREATED_TITLE = By.cssSelector("[data-testid='customer-created-title']");

    public CustomerCreateDialog(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME));
    }

    public CustomerCreateDialog fill(
            String firstName,
            String lastName,
            String email,
            String phone,
            String address) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(EMAIL, email);
        type(PHONE, phone);
        type(ADDRESS, address);
        return this;
    }

    public void submitAndWaitForSuccess() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(CREATED_TITLE));
    }

    public String successTitleText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(CREATED_TITLE)).getText().trim();
    }

    private void type(By locator, String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(value);
    }
}
