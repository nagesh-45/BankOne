package com.bankone.api;

import com.bankone.support.ApiTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApiTest extends ApiTestBase {

    @Test
    void listCreateAndUpdateEmployee() throws Exception {
        mockMvc.perform(get("/users")
                        .headers(bearer(adminToken))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        String username = uniqueUsername("emp");
        String created = mockMvc.perform(post("/users")
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "userType":"EMPLOYEE",
                                  "roleNames":["TELLER"],
                                  "username":"%s",
                                  "password":"Teller@123",
                                  "firstName":"Tel",
                                  "lastName":"Ler",
                                  "email":"%s"
                                }
                                """.formatted(username, uniqueEmail("emp"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = objectMapper.readTree(created).get("userId").asLong();

        mockMvc.perform(put("/users/" + userId)
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "firstName":"TelUpdated",
                                  "lastName":"Ler",
                                  "email":"%s",
                                  "enabled":true,
                                  "roleNames":["TELLER"]
                                }
                                """.formatted(objectMapper.readTree(created).get("email").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("TelUpdated"));
    }
}
