package com.bankone.e2e;

import com.bankone.e2e.pages.CustomerCreateDialog;
import com.bankone.e2e.pages.LoginPage;
import com.bankone.e2e.pages.ManagementPage;
import com.bankone.e2e.support.TestConfig;
import com.bankone.e2e.support.WebDriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateCustomerE2ETest {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = WebDriverFactory.create();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void adminCanLoginAndCreateCustomer() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String email = "e2e.customer." + suffix + "@bankone.test";
        // 10-digit phone; keep unique-ish within common DB unique constraints
        String phone = "9" + String.format("%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));

        new LoginPage(driver)
                .open(TestConfig.baseUrl())
                .login(TestConfig.username(), TestConfig.password());

        CustomerCreateDialog dialog = new ManagementPage(driver)
                .open(TestConfig.baseUrl())
                .openCreateCustomer();

        dialog.fill("E2E", "Customer", email, phone, "1 E2E Street")
                .submitAndWaitForSuccess();

        assertEquals("Customer created", dialog.successTitleText());
    }
}
