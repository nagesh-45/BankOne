package com.bankone.api;

import com.bankone.support.ApiTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortalApiTest extends ApiTestBase {

    @Test
    void portalAccountsBeneficiariesAndImmediateTransfer() throws Exception {
        String portalUser = uniqueUsername("pu");
        String portalPass = "Portal@123";
        JsonNode fromCustomer = createCustomerWithPortal(adminToken, portalUser, portalPass);
        long fromCustomerId = fromCustomer.get("customerId").asLong();
        long fromAccountId = firstAccountIdForCustomer(adminToken, fromCustomerId);

        JsonNode toCustomer = createCustomerWithAccount(adminToken);
        long toAccountId = firstAccountIdForCustomer(adminToken, toCustomer.get("customerId").asLong());
        String toAccountNumber = objectMapper.readTree(
                        mockMvc.perform(get("/accounts/" + toAccountId).headers(bearer(adminToken)))
                                .andReturn().getResponse().getContentAsString())
                .get("accountNumber").asText();

        String portalToken = login(portalUser, portalPass);

        mockMvc.perform(get("/portal/accounts").headers(bearer(portalToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].accountId").value((int) fromAccountId));

        mockMvc.perform(get("/portal/accounts/" + fromAccountId).headers(bearer(portalToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/portal/accounts/" + fromAccountId + "/transactions")
                        .headers(bearer(portalToken))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        String beneficiaryJson = mockMvc.perform(post("/portal/beneficiaries")
                        .headers(bearer(portalToken))
                        .content("""
                                {
                                  "nickname":"Friend",
                                  "bankType":"SAME_BANK",
                                  "accountNumber":"%s",
                                  "accountHolderName":"Friend Name",
                                  "ifsc":"BANK0001",
                                  "bankName":"BankOne"
                                }
                                """.formatted(toAccountNumber)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.beneficiaryId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long beneficiaryId = objectMapper.readTree(beneficiaryJson).get("beneficiaryId").asLong();

        mockMvc.perform(get("/portal/beneficiaries").headers(bearer(portalToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // No threshold → immediate execute for SAME_BANK
        mockMvc.perform(post("/portal/accounts/" + fromAccountId + "/transfer")
                        .headers(bearer(portalToken))
                        .content("""
                                {
                                  "amount":100,
                                  "beneficiaryId":%d,
                                  "narration":"portal pay"
                                }
                                """.formatted(beneficiaryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("EXECUTED"));

        mockMvc.perform(delete("/portal/beneficiaries/" + beneficiaryId).headers(bearer(portalToken)))
                .andExpect(status().isNoContent());
    }
}
