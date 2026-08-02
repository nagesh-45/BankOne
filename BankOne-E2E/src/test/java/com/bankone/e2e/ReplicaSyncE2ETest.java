package com.bankone.e2e;

import com.bankone.e2e.pages.CustomerCreateDialog;
import com.bankone.e2e.pages.EmployeeCreateDialog;
import com.bankone.e2e.pages.LoginPage;
import com.bankone.e2e.pages.ManagementPage;
import com.bankone.e2e.support.BankOneApi;
import com.bankone.e2e.support.ReplicaDb;
import com.bankone.e2e.support.TestConfig;
import com.bankone.e2e.support.WebDriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Creates customer / account / employee via UI, deposit + withdraw + transfer on write API,
 * asserts read replica lacks rows until Management → Sync replica now.
 *
 * <p>Money movements use the write API (not Accounts autocomplete) because account search is
 * {@code @Transactional(readOnly=true)} and would only see rows after sync — defeating the lag check.
 *
 * Prerequisites: Liberty :9080 (replica enabled), Angular :4200, bankone + bankone_read.
 *
 *   export BANKONE_REPLICA_E2E=true
 *   cd BankOne-E2E && mvn -B -Dtest=ReplicaSyncE2ETest test
 */
@EnabledIfEnvironmentVariable(named = "BANKONE_REPLICA_E2E", matches = "true")
class ReplicaSyncE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        assumeTrue(TestConfig.baseUrl().contains("localhost")
                        || TestConfig.baseUrl().contains("127.0.0.1"),
                "Replica E2E expects local UI");
        assumeTrue(canPingReplica(), "bankone_read must be reachable for replica E2E");
        driver = WebDriverFactory.create();
        wait = new WebDriverWait(driver, Duration.ofSeconds(40));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Write-path data appears on replica only after Sync replica now")
    void writePathDataMissingOnReplicaUntilSync() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String phoneA = uniquePhone();
        String phoneB = uniquePhone();
        String emailA = "e2e.rep.a." + suffix + "@bankone.test";
        String emailB = "e2e.rep.b." + suffix + "@bankone.test";
        String lastA = "RepA" + suffix.substring(Math.max(0, suffix.length() - 4));
        String lastB = "RepB" + suffix.substring(Math.max(0, suffix.length() - 4));
        String empUser = "emp" + suffix.substring(Math.max(0, suffix.length() - 8));
        String empEmail = "e2e.emp." + suffix + "@bankone.test";

        new LoginPage(driver)
                .open(TestConfig.baseUrl())
                .login(TestConfig.username(), TestConfig.password());

        ManagementPage management = new ManagementPage(driver).open(TestConfig.baseUrl());
        // Reset lag window: next scheduled sync is ~120s later — keep writes under that.
        management.syncReplicaNow();

        // --- Customer A (opens account) ---
        CustomerCreateDialog createA = management.openCreateCustomer();
        createA.fill("E2E", lastA, emailA, phoneA, "1 Replica St")
                .submitAndWaitForSuccess();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='customer-created-view']"))).click();
        wait.until(ExpectedConditions.urlContains("/app/customers"));

        // --- Customer B (transfer target) ---
        management.open(TestConfig.baseUrl());
        CustomerCreateDialog createB = management.openCreateCustomer();
        createB.fill("E2E", lastB, emailB, phoneB, "2 Replica St")
                .submitAndWaitForSuccess();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//mat-dialog-actions//button[contains(.,'Done')]"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("[data-testid='customer-created-title']")));

        // --- Employee user ---
        management.open(TestConfig.baseUrl());
        EmployeeCreateDialog emp = management.openCreateEmployee();
        emp.continueAsEmployee()
                .fill("E2E", "Staff" + lastA, empUser, "Staff@123", empEmail)
                .submitAndWaitForSuccess();

        // --- Deposit + withdraw on A, transfer A → B (write API) ---
        long accountA = ReplicaDb.accountIdOnWriteForCustomerEmail(emailA);
        long accountB = ReplicaDb.accountIdOnWriteForCustomerEmail(emailB);
        assertTrue(accountA > 0, "Customer A must have an account on write DB");
        assertTrue(accountB > 0, "Customer B must have an account on write DB");

        BankOneApi api = new BankOneApi();
        String token = api.login(TestConfig.username(), TestConfig.password());
        api.deposit(token, accountA, "200");
        api.withdraw(token, accountA, "25");
        api.transfer(token, accountA, accountB, "10");

        // Write DB must have the new rows
        assertTrue(ReplicaDb.customerExistsOnWrite(emailA), "Customer A on write DB");
        assertTrue(ReplicaDb.customerExistsOnWrite(emailB), "Customer B on write DB");
        assertTrue(ReplicaDb.userExistsOnWrite(empUser), "Employee on write DB");
        long writeTx = ReplicaDb.transactionCountOnWriteForCustomerEmail(emailA);
        assertTrue(writeTx >= 1, "Expected money movements on write DB for A, got " + writeTx);

        // Replica must NOT have them yet (before the Sync we click below)
        assertFalse(ReplicaDb.customerExistsOnReplica(emailA),
                "Customer A must NOT be on replica before sync");
        assertFalse(ReplicaDb.customerExistsOnReplica(emailB),
                "Customer B must NOT be on replica before sync");
        assertFalse(ReplicaDb.userExistsOnReplica(empUser),
                "Employee must NOT be on replica before sync");
        assertTrue(
                ReplicaDb.transactionCountOnReplicaForCustomerEmail(emailA) < writeTx,
                "Replica should lag write txs for customer A before sync"
        );

        // --- Sync via UI ---
        management.open(TestConfig.baseUrl()).syncReplicaNow();

        assertTrue(ReplicaDb.customerExistsOnReplica(emailA),
                "Customer A on replica after sync");
        assertTrue(ReplicaDb.customerExistsOnReplica(emailB),
                "Customer B on replica after sync");
        assertTrue(ReplicaDb.userExistsOnReplica(empUser),
                "Employee on replica after sync");
        assertTrue(
                ReplicaDb.transactionCountOnReplicaForCustomerEmail(emailA) >= writeTx,
                "Replica txs for A should match write after sync"
        );
    }

    private static String uniquePhone() {
        return "9" + String.format("%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));
    }

    private static boolean canPingReplica() {
        try {
            ReplicaDb.customerExistsOnReplica("nobody@bankone.invalid");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
