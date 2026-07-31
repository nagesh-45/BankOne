package com.bankone.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class ApiTestBase {

    private static final AtomicLong PHONE_SEQ = new AtomicLong(ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_000_000_000L));

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected String adminToken;

    @BeforeEach
    void loginAdmin() throws Exception {
        adminToken = login("admin", "Admin@123");
    }

    protected String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    protected HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID().toString().substring(0, 8) + "@bankone.test";
    }

    protected String uniquePhone() {
        return "9" + String.format("%09d", Math.floorMod(PHONE_SEQ.incrementAndGet(), 1_000_000_000L));
    }

    protected String uniqueUsername(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    protected JsonNode createCustomerWithAccount(String token) throws Exception {
        String email = uniqueEmail("cust");
        String phone = uniquePhone();
        String body = """
                {
                  "firstName":"Api",
                  "lastName":"Test",
                  "email":"%s",
                  "phoneNumber":"%s",
                  "address":"1 Test Lane",
                  "status":"ACTIVE",
                  "branchCode":"0001",
                  "accountType":"SAVINGS",
                  "currencyCode":"INR",
                  "openingDeposit":1000
                }
                """.formatted(email, phone);
        MvcResult result = mockMvc.perform(post("/customers")
                        .headers(bearer(token))
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected JsonNode createCustomerWithPortal(String token, String portalUser, String portalPass) throws Exception {
        String email = uniqueEmail("portal");
        String phone = uniquePhone();
        String body = """
                {
                  "firstName":"Portal",
                  "lastName":"User",
                  "email":"%s",
                  "phoneNumber":"%s",
                  "address":"2 Portal Lane",
                  "status":"ACTIVE",
                  "branchCode":"0001",
                  "accountType":"SAVINGS",
                  "currencyCode":"INR",
                  "openingDeposit":5000,
                  "portalUsername":"%s",
                  "portalPassword":"%s"
                }
                """.formatted(email, phone, portalUser, portalPass);
        MvcResult result = mockMvc.perform(post("/customers")
                        .headers(bearer(token))
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected long firstAccountIdForCustomer(String token, long customerId) throws Exception {
        MvcResult result = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/accounts/customer/" + customerId)
                                .headers(bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode content = root.has("content") ? root.get("content") : root;
        return content.get(0).get("accountId").asLong();
    }

    protected ResultActions postJson(String path, String token, String json) throws Exception {
        return mockMvc.perform(post(path).headers(bearer(token)).content(json));
    }
}
