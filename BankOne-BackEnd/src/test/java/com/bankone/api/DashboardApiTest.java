package com.bankone.api;

import com.bankone.support.ApiTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardApiTest extends ApiTestBase {

    @Test
    void dashboardReturnsOk() throws Exception {
        mockMvc.perform(get("/dashboard").headers(bearer(adminToken)))
                .andExpect(status().isOk());
    }
}
