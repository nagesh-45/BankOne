package com.bankone.api;

import com.bankone.support.ApiTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleApiTest extends ApiTestBase {

    @Test
    void listCatalogGetCreateAndUpdateRole() throws Exception {
        mockMvc.perform(get("/roles/access-catalog").headers(bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/roles").headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        String roleName = "ROLE_" + uniqueUsername("R").toUpperCase();
        String created = mockMvc.perform(post("/roles")
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "roleName":"%s",
                                  "description":"test role",
                                  "accessCodes":["CUSTOMERS_READ","ACCOUNTS_READ"]
                                }
                                """.formatted(roleName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleName").value(roleName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long roleId = objectMapper.readTree(created).has("roleId")
                ? objectMapper.readTree(created).get("roleId").asLong()
                : objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/roles/" + roleId).headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value(roleName));

        mockMvc.perform(put("/roles/" + roleId)
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "description":"updated test role",
                                  "accessCodes":["CUSTOMERS_READ"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("updated test role"));
    }
}
