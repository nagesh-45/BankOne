package com.bankone.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By USERNAME = By.name("username");
    private static final By PASSWORD = By.name("password");
    private static final By SUBMIT = By.cssSelector("[data-testid='login-submit']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME));
        return this;
    }

    public void login(String username, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(USERNAME)).clear();
        driver.findElement(USERNAME).sendKeys(username);
        driver.findElement(PASSWORD).clear();
        driver.findElement(PASSWORD).sendKeys(password);
        driver.findElement(SUBMIT).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/app/"),
                ExpectedConditions.urlContains("/portal/")
        ));
    }
}
