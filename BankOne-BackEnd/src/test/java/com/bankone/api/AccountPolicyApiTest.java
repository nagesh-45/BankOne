package com.bankone.api;

import com.bankone.support.ApiTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountPolicyApiTest extends ApiTestBase {

    @Test
    void listPoliciesAndGetByType() throws Exception {
        mockMvc.perform(get("/account-policies/all").headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/account-policies")
                        .headers(bearer(adminToken))
                        .param("accountType", "SAVINGS")
                        .param("currencyCode", "INR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountType").value("SAVINGS"));
    }

    @Test
    void updateExistingSavingsPolicy() throws Exception {
        String listJson = mockMvc.perform(get("/account-policies/all").headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode policies = objectMapper.readTree(listJson);
        JsonNode savings = null;
        for (JsonNode p : policies) {
            if ("SAVINGS".equals(p.path("accountType").asText())
                    && "INR".equals(p.path("currencyCode").asText())) {
                savings = p;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertNotNull(savings, "SAVINGS/INR policy missing");

        long policyId = savings.get("policyId").asLong();
        String effectiveFrom = savings.hasNonNull("effectiveFrom")
                ? savings.get("effectiveFrom").asText()
                : java.time.LocalDateTime.now().minusYears(1).toString();

        mockMvc.perform(put("/account-policies/" + policyId)
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "openingDepositRequired":true,
                                  "requiredOpeningDeposit":1000,
                                  "minimumBalance":500,
                                  "active":true,
                                  "effectiveFrom":"%s"
                                }
                                """.formatted(effectiveFrom)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiredOpeningDeposit").value(1000.0));
    }
}
