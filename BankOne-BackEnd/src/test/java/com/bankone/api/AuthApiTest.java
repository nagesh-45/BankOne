package com.bankone.api;

import com.bankone.support.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends ApiTestBase {

    @Test
    void loginSucceedsForAdmin() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"Admin@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void loginFailsForBadPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsProfile() throws Exception {
        mockMvc.perform(get("/auth/me").headers(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void logoutReturnsNoContent() throws Exception {
        mockMvc.perform(post("/auth/logout").headers(bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePasswordRejectsWrongCurrent() throws Exception {
        mockMvc.perform(put("/auth/password")
                        .headers(bearer(adminToken))
                        .content("""
                                {
                                  "currentPassword":"not-the-password",
                                  "newPassword":"Admin@1234",
                                  "confirmPassword":"Admin@1234"
                                }
                                """))
                .andExpect(status().is4xxClientError());
    }
}
