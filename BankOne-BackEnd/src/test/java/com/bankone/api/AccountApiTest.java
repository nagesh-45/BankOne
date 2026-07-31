package com.bankone.api;

import com.bankone.support.ApiTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountApiTest extends ApiTestBase {

    @Test
    void openListGetDepositWithdrawTransferAndTransactions() throws Exception {
        JsonNode customerA = createCustomerWithAccount(adminToken);
        JsonNode customerB = createCustomerWithAccount(adminToken);
        long customerIdA = customerA.get("customerId").asLong();
        long customerIdB = customerB.get("customerId").asLong();
        long fromId = firstAccountIdForCustomer(adminToken, customerIdA);
        long toId = firstAccountIdForCustomer(adminToken, customerIdB);

        mockMvc.perform(get("/accounts/" + fromId).headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value((int) fromId))
                .andExpect(jsonPath("$.availableBalance").value(1000.0));

        mockMvc.perform(get("/accounts")
                        .headers(bearer(adminToken))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(post("/accounts/" + fromId + "/deposit")
                        .headers(bearer(adminToken))
                        .content("{\"amount\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(1500.0));

        mockMvc.perform(post("/accounts/" + fromId + "/withdraw")
                        .headers(bearer(adminToken))
                        .content("{\"amount\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(1300.0));

        mockMvc.perform(post("/accounts/" + fromId + "/transfer")
                        .headers(bearer(adminToken))
                        .content("""
                                {"toAccountId":%d,"amount":100}
                                """.formatted(toId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(1200.0));

        mockMvc.perform(get("/accounts/" + toId).headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(1100.0));

        mockMvc.perform(get("/accounts/" + fromId + "/transactions")
                        .headers(bearer(adminToken))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(post("/accounts")
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "customerId":%d,
                                  "branchCode":"0001",
                                  "accountType":"CURRENT",
                                  "currencyCode":"INR",
                                  "openingDeposit":5000,
                                  "createdBy":"admin"
                                }
                                """.formatted(customerIdA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountType").value("CURRENT"));
    }

    @Test
    void withdrawRejectsInsufficientFunds() throws Exception {
        JsonNode customer = createCustomerWithAccount(adminToken);
        long accountId = firstAccountIdForCustomer(adminToken, customer.get("customerId").asLong());

        mockMvc.perform(post("/accounts/" + accountId + "/withdraw")
                        .headers(bearer(adminToken))
                        .content("{\"amount\":999999}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateStatus() throws Exception {
        JsonNode customer = createCustomerWithAccount(adminToken);
        long accountId = firstAccountIdForCustomer(adminToken, customer.get("customerId").asLong());

        mockMvc.perform(put("/accounts/" + accountId + "/status")
                        .headers(bearer(adminToken))
                        .content("{\"status\":\"FROZEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));
    }
}
