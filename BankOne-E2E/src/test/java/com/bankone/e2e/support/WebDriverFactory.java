package com.bankone.e2e.support;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public final class WebDriverFactory {

    private WebDriverFactory() {
    }

    public static WebDriver create() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (TestConfig.headless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1440,900");
        }
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        if (!TestConfig.headless()) {
            driver.manage().window().maximize();
        }
        return driver;
    }
}
