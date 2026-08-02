package com.bankone.e2e.pages;

import com.bankone.e2e.support.DomHelpers;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
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
        DomHelpers.dismissViteErrorOverlay(driver);
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME));
    }

    public CustomerCreateDialog fill(
            String firstName,
            String lastName,
            String email,
            String phone,
            String address) {
        DomHelpers.setAngularInput(driver, wait, FIRST_NAME, firstName);
        DomHelpers.setAngularInput(driver, wait, LAST_NAME, lastName);
        DomHelpers.setAngularInput(driver, wait, EMAIL, email);
        DomHelpers.setAngularInput(driver, wait, PHONE, phone);
        DomHelpers.setAngularInput(driver, wait, ADDRESS, address);
        return this;
    }

    public void submitAndWaitForSuccess() {
        DomHelpers.dismissViteErrorOverlay(driver);
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT)).click();
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(CREATED_TITLE));
        } catch (TimeoutException ex) {
            String snack = DomHelpers.snackbarText(driver);
            throw new TimeoutException(
                    "Customer create did not reach success screen. Snackbar='" + snack + "'",
                    ex);
        }
    }

    public String successTitleText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(CREATED_TITLE)).getText().trim();
    }
}
