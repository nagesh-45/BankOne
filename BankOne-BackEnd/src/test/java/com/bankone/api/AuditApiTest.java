package com.bankone.api;

import com.bankone.support.ApiTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditApiTest extends ApiTestBase {

    @Test
    void listAuditEventsAndTransferApprovals() throws Exception {
        mockMvc.perform(get("/audit/events")
                        .headers(bearer(adminToken))
                        .param("page", "0")
                        .param("size", "25"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/audit/transfer-approvals").headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
