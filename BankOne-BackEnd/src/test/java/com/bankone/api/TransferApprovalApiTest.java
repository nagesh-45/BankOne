package com.bankone.api;

import com.bankone.support.ApiTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferApprovalApiTest extends ApiTestBase {

    @Test
    void portalTransferAboveThresholdCreatesPendingAndAdminCanApprove() throws Exception {
        String portalUser = uniqueUsername("pt");
        String portalPass = "Portal@123";
        JsonNode fromCustomer = createCustomerWithPortal(adminToken, portalUser, portalPass);
        long fromCustomerId = fromCustomer.get("customerId").asLong();

        mockMvc.perform(put("/customers/" + fromCustomerId)
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "firstName":"%s",
                                  "lastName":"%s",
                                  "email":"%s",
                                  "phoneNumber":"%s",
                                  "address":"%s",
                                  "status":"ACTIVE",
                                  "transferApprovalThreshold":1000
                                }
                                """.formatted(
                                fromCustomer.get("firstName").asText(),
                                fromCustomer.get("lastName").asText(),
                                fromCustomer.get("email").asText(),
                                fromCustomer.get("phoneNumber").asText(),
                                fromCustomer.get("address").asText())))
                .andExpect(status().isOk());

        JsonNode toCustomer = createCustomerWithAccount(adminToken);
        long toAccountId = firstAccountIdForCustomer(adminToken, toCustomer.get("customerId").asLong());
        String toAccountNumber = mockMvc.perform(get("/accounts/" + toAccountId).headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String accountNumber = objectMapper.readTree(toAccountNumber).get("accountNumber").asText();

        long fromAccountId = firstAccountIdForCustomer(adminToken, fromCustomerId);
        String portalToken = login(portalUser, portalPass);

        String outcomeJson = mockMvc.perform(post("/portal/accounts/" + fromAccountId + "/transfer")
                        .headers(bearer(portalToken))
                        .content("""
                                {
                                  "amount":1500,
                                  "toAccountNumber":"%s",
                                  "narration":"needs approval"
                                }
                                """.formatted(accountNumber)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("PENDING_APPROVAL"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long transferRequestId = objectMapper.readTree(outcomeJson).get("transferRequestId").asLong();

        mockMvc.perform(get("/transfer-approvals").headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(post("/transfer-approvals/" + transferRequestId + "/approve")
                        .headers(bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/transfer-approvals/my-history").headers(bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void rejectPendingTransfer() throws Exception {
        String portalUser = uniqueUsername("rj");
        String portalPass = "Portal@123";
        JsonNode fromCustomer = createCustomerWithPortal(adminToken, portalUser, portalPass);
        long fromCustomerId = fromCustomer.get("customerId").asLong();

        mockMvc.perform(put("/customers/" + fromCustomerId)
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "firstName":"%s",
                                  "lastName":"%s",
                                  "email":"%s",
                                  "phoneNumber":"%s",
                                  "address":"%s",
                                  "status":"ACTIVE",
                                  "transferApprovalThreshold":500
                                }
                                """.formatted(
                                fromCustomer.get("firstName").asText(),
                                fromCustomer.get("lastName").asText(),
                                fromCustomer.get("email").asText(),
                                fromCustomer.get("phoneNumber").asText(),
                                fromCustomer.get("address").asText())))
                .andExpect(status().isOk());

        JsonNode toCustomer = createCustomerWithAccount(adminToken);
        long toAccountId = firstAccountIdForCustomer(adminToken, toCustomer.get("customerId").asLong());
        String accountNumber = objectMapper.readTree(
                        mockMvc.perform(get("/accounts/" + toAccountId).headers(bearer(adminToken)))
                                .andReturn().getResponse().getContentAsString())
                .get("accountNumber").asText();

        long fromAccountId = firstAccountIdForCustomer(adminToken, fromCustomerId);
        String portalToken = login(portalUser, portalPass);

        long transferRequestId = objectMapper.readTree(
                        mockMvc.perform(post("/portal/accounts/" + fromAccountId + "/transfer")
                                        .headers(bearer(portalToken))
                                        .content("""
                                                {"amount":800,"toAccountNumber":"%s","narration":"reject me"}
                                                """.formatted(accountNumber)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.outcome").value("PENDING_APPROVAL"))
                                .andReturn().getResponse().getContentAsString())
                .get("transferRequestId").asLong();

        mockMvc.perform(post("/transfer-approvals/" + transferRequestId + "/reject")
                        .headers(bearer(adminToken))
                        .content("{\"rejectionReason\":\"test reject\"}"))
                .andExpect(status().isOk());
    }
}
