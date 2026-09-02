package com.example.app.controller;

import com.example.app.config.CustomUserDetailsService;
import com.example.app.config.SecurityConfig;
import com.example.app.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void loginPage_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sign in")));
    }

    @Test
    void loginPage_withErrorParam_showsErrorMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid username or password")));
    }

    @Test
    void registerPage_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }
}
