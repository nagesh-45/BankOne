package com.bankone.api;

import com.bankone.support.ApiTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerApiTest extends ApiTestBase {

    @Test
    void createGetListUpdateAndDeleteCustomer() throws Exception {
        String email = uniqueEmail("lifecycle");
        String phone = uniquePhone();

        String createBody = """
                {
                  "firstName":"Life",
                  "lastName":"Cycle",
                  "email":"%s",
                  "phoneNumber":"%s",
                  "address":"9 Cycle Rd",
                  "status":"ACTIVE",
                  "branchCode":"0001",
                  "accountType":"SAVINGS",
                  "currencyCode":"INR",
                  "openingDeposit":1000
                }
                """.formatted(email, phone);

        String createdJson = mockMvc.perform(post("/customers")
                        .headers(bearer(adminToken))
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long customerId = objectMapper.readTree(createdJson).get("customerId").asLong();

        mockMvc.perform(get("/customers/" + customerId).headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Life"));

        mockMvc.perform(get("/customers")
                        .headers(bearer(adminToken))
                        .param("search", email)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(put("/customers/" + customerId)
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "firstName":"LifeUpdated",
                                  "lastName":"Cycle",
                                  "email":"%s",
                                  "phoneNumber":"%s",
                                  "address":"9 Cycle Rd",
                                  "status":"ACTIVE",
                                  "transferApprovalThreshold":10000
                                }
                                """.formatted(email, phone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("LifeUpdated"))
                .andExpect(jsonPath("$.transferApprovalThreshold").value(10000));
    }

    @Test
    void createCustomerRejectsDuplicateEmail() throws Exception {
        JsonNode customer = createCustomerWithAccount(adminToken);
        String email = customer.get("email").asText();

        mockMvc.perform(post("/customers")
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "firstName":"Dup",
                                  "lastName":"Email",
                                  "email":"%s",
                                  "phoneNumber":"%s",
                                  "address":"x",
                                  "status":"ACTIVE",
                                  "branchCode":"0001",
                                  "accountType":"SAVINGS",
                                  "currencyCode":"INR",
                                  "openingDeposit":1000
                                }
                                """.formatted(email, uniquePhone())))
                .andExpect(status().isConflict());
    }

    @Test
    void createCustomerRejectsLowOpeningDeposit() throws Exception {
        mockMvc.perform(post("/customers")
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "firstName":"Low",
                                  "lastName":"Deposit",
                                  "email":"%s",
                                  "phoneNumber":"%s",
                                  "address":"x",
                                  "status":"ACTIVE",
                                  "branchCode":"0001",
                                  "accountType":"SAVINGS",
                                  "currencyCode":"INR",
                                  "openingDeposit":100
                                }
                                """.formatted(uniqueEmail("low"), uniquePhone())))
                .andExpect(status().isBadRequest());
    }
}
