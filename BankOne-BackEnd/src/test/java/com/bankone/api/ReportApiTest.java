package com.bankone.api;

import com.bankone.support.ApiTestBase;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportApiTest extends ApiTestBase {

    @Test
    void jsonReportsReturnOk() throws Exception {
        String from = LocalDate.now().minusDays(30).toString();
        String to = LocalDate.now().toString();

        mockMvc.perform(get("/reports/transaction-trends")
                        .headers(bearer(adminToken))
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reports/account-mix").headers(bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reports/approvals")
                        .headers(bearer(adminToken))
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk());
    }

    @Test
    void pdfReportsReturnOk() throws Exception {
        String from = LocalDate.now().minusDays(30).toString();
        String to = LocalDate.now().toString();

        mockMvc.perform(get("/reports/transaction-trends/pdf")
                        .headers(bearer(adminToken))
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reports/account-mix/pdf").headers(bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reports/approvals/pdf")
                        .headers(bearer(adminToken))
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk());
    }
}
