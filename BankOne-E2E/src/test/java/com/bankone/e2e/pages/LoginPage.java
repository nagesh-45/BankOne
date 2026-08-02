package com.bankone.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.IntSupplier;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By USERNAME = By.name("username");
    private static final By PASSWORD = By.name("password");
    private static final By SUBMIT = By.cssSelector("[data-testid='login-submit']");
    private static final By SNACKBAR = By.cssSelector(
            "simple-snack-bar, .mat-mdc-simple-snack-bar, .mdc-snackbar__label");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl);
        dismissViteErrorOverlay();
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME));
        return this;
    }

    public void login(String username, String password) {
        fillCredentials(username, password);
        submitLoginForm();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/app/"),
                ExpectedConditions.urlContains("/portal/")
        ));
    }

    /**
     * Failed login: sync Angular ngModel via native value setter, submit form,
     * then wait until the HTTP attempt finishes (status count grows, loading ends, or snackbar).
     */
    public void attemptFailedLogin(String username, String password, IntSupplier loginStatusCount) {
        dismissViteErrorOverlay();
        int before = loginStatusCount.getAsInt();
        fillCredentials(username, password);
        assertFieldsHaveValues(username, password);
        submitLoginForm();

        wait.until(d -> {
            if (loginStatusCount.getAsInt() > before) {
                return true;
            }
            // Fallback: loading finished after having started, or snackbar from early validation/API
            WebElement btn = d.findElement(SUBMIT);
            boolean disabled = !btn.isEnabled()
                    || "true".equalsIgnoreCase(String.valueOf(btn.getAttribute("disabled")))
                    || "true".equalsIgnoreCase(String.valueOf(btn.getAttribute("aria-disabled")));
            if (disabled) {
                return false; // still in flight — keep waiting for status or re-enable
            }
            return !d.findElements(SNACKBAR).isEmpty() && loginStatusCount.getAsInt() > before;
        });

        // If request finished, button should be enabled again
        wait.until(d -> {
            WebElement btn = d.findElement(SUBMIT);
            return btn.isEnabled()
                    && !"true".equalsIgnoreCase(String.valueOf(btn.getAttribute("aria-disabled")));
        });
    }

    /** Overload when no network probe — wait for snackbar / loading cycle. */
    public void attemptFailedLogin(String username, String password) {
        dismissViteErrorOverlay();
        fillCredentials(username, password);
        assertFieldsHaveValues(username, password);
        submitLoginForm();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(SNACKBAR),
                d -> {
                    WebElement btn = d.findElement(SUBMIT);
                    return !btn.isEnabled()
                            || "true".equalsIgnoreCase(String.valueOf(btn.getAttribute("disabled")));
                }
        ));
        wait.until(d -> {
            WebElement btn = d.findElement(SUBMIT);
            return btn.isEnabled()
                    && !"true".equalsIgnoreCase(String.valueOf(btn.getAttribute("aria-disabled")));
        });
    }

    private void fillCredentials(String username, String password) {
        setAngularInput(USERNAME, username);
        setAngularInput(PASSWORD, password);
    }

    /**
     * Angular ngModel ignores naive .value= assignments unless the native setter is used.
     */
    private void setAngularInput(By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript(
                """
                const input = arguments[0];
                const value = arguments[1];
                input.focus();
                const proto = window.HTMLInputElement.prototype;
                const desc = Object.getOwnPropertyDescriptor(proto, 'value');
                desc.set.call(input, value);
                input.dispatchEvent(new Event('input', { bubbles: true }));
                input.dispatchEvent(new Event('change', { bubbles: true }));
                """,
                el,
                value
        );
    }

    private void assertFieldsHaveValues(String username, String password) {
        String actualUser = driver.findElement(USERNAME).getAttribute("value");
        String actualPass = driver.findElement(PASSWORD).getAttribute("value");
        if (!username.equals(actualUser) || !password.equals(actualPass)) {
            throw new IllegalStateException(
                    "Login fields not set for Angular. user='" + actualUser
                            + "' passLen=" + (actualPass == null ? -1 : actualPass.length()));
        }
    }

    private void submitLoginForm() {
        dismissViteErrorOverlay();
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(SUBMIT));
        ((JavascriptExecutor) driver).executeScript(
                """
                const btn = arguments[0];
                const form = btn.closest('form');
                if (form && typeof form.requestSubmit === 'function') {
                  form.requestSubmit(btn);
                } else if (form) {
                  form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
                } else {
                  btn.click();
                }
                """,
                btn
        );
    }

    public void dismissViteErrorOverlay() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('vite-error-overlay').forEach(e => e.remove());"
            );
        } catch (Exception ignored) {
            // page may not be ready
        }
    }

    public boolean isStillOnLoginPage() {
        String url = driver.getCurrentUrl();
        return !url.contains("/app/") && !url.contains("/portal/");
    }

    public boolean snackbarVisibleWithText(String fragment) {
        try {
            wait.withTimeout(Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(SNACKBAR));
            String text = driver.findElement(SNACKBAR).getText();
            return text != null && text.toLowerCase().contains(fragment.toLowerCase());
        } catch (Exception ex) {
            return false;
        }
    }
}
