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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginE2ETest {

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
    void badPasswordStaysOnLogin() {
        new LoginPage(driver).open(TestConfig.baseUrl());
        driver.findElement(By.name("username")).clear();
        driver.findElement(By.name("username")).sendKeys(TestConfig.username());
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys("definitely-wrong");
        driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        assertFalse(driver.getCurrentUrl().contains("/app/"),
                "Should not enter app after bad password");
    }

    @Test
    void goodPasswordEntersApp() {
        new LoginPage(driver)
                .open(TestConfig.baseUrl())
                .login(TestConfig.username(), TestConfig.password());
        assertTrue(driver.getCurrentUrl().contains("/app/"));
    }
}
