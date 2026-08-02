package com.bankone.e2e.pages;

import com.bankone.e2e.support.DomHelpers;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class EmployeeCreateDialog {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public EmployeeCreateDialog(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        DomHelpers.dismissViteErrorOverlay(driver);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("userType")));
    }

    public EmployeeCreateDialog continueAsEmployee() {
        // EMPLOYEE is default; click Continue
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//mat-dialog-actions//button[contains(.,'Continue')]"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        return this;
    }

    public EmployeeCreateDialog fill(
            String firstName,
            String lastName,
            String username,
            String password,
            String email
    ) {
        DomHelpers.setAngularInput(driver, wait, By.name("firstName"), firstName);
        DomHelpers.setAngularInput(driver, wait, By.name("lastName"), lastName);
        DomHelpers.setAngularInput(driver, wait, By.name("username"), username);
        DomHelpers.setAngularInput(driver, wait, By.name("password"), password);
        DomHelpers.setAngularInput(driver, wait, By.name("email"), email);
        // roleNames defaults to EMPLOYEE in the dialog — no mat-select click needed
        return this;
    }

    public void submitAndWaitForSuccess() {
        DomHelpers.dismissViteErrorOverlay(driver);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='employee-create-submit']"))).click();
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[contains(.,'Employee created')]")));
        } catch (TimeoutException ex) {
            String snack = DomHelpers.snackbarText(driver);
            throw new TimeoutException(
                    "Employee create did not reach success screen. Snackbar='" + snack + "'",
                    ex);
        }
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//mat-dialog-actions//button[contains(.,'Done')]"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//h2[contains(.,'Employee created')]")));
    }
}
