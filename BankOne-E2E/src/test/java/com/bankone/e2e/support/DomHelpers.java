package com.bankone.e2e.support;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Shared Selenium helpers for Angular Material + Vite.
 */
public final class DomHelpers {

    private DomHelpers() {
    }

    public static void dismissViteErrorOverlay(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('vite-error-overlay').forEach(e => e.remove());"
            );
        } catch (Exception ignored) {
            // page may not be ready
        }
    }

    /**
     * Sets an input so Angular ngModel / ngModelChange picks up the value.
     */
    public static void setAngularInput(WebDriver driver, WebDriverWait wait, By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript(
                """
                const input = arguments[0];
                const value = arguments[1];
                input.focus();
                const tag = (input.tagName || '').toLowerCase();
                const proto = tag === 'textarea'
                    ? window.HTMLTextAreaElement.prototype
                    : window.HTMLInputElement.prototype;
                const desc = Object.getOwnPropertyDescriptor(proto, 'value');
                desc.set.call(input, value);
                input.dispatchEvent(new Event('input', { bubbles: true }));
                input.dispatchEvent(new Event('change', { bubbles: true }));
                """,
                el,
                value
        );
    }

    public static String snackbarText(WebDriver driver) {
        try {
            var els = driver.findElements(By.cssSelector(
                    "simple-snack-bar, .mat-mdc-simple-snack-bar, .mdc-snackbar__label"));
            if (els.isEmpty()) {
                return "";
            }
            String text = els.get(0).getText();
            return text == null ? "" : text.trim();
        } catch (Exception ex) {
            return "";
        }
    }
}
