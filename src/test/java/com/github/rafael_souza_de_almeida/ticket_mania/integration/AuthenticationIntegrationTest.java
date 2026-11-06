package com.github.rafael_souza_de_almeida.ticket_mania.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.AuthenticationDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.RegisterDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrationLoginAndAuthenticatedOrderLookupPersistTheUser() throws Exception {
        RegisterDto registration = new RegisterDto("Ticket Buyer", "buyer-" + System.nanoTime() + "@test.com", "secret123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(registration)))
                .andExpect(status().isCreated());

        var persistedUser = userRepository.findByEmail(registration.email());
        assertTrue(passwordEncoder.matches(registration.password(), persistedUser.getPassword()));
        assertNotEquals(registration.password(), persistedUser.getPassword());
        assertTrue(persistedUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));

        String loginJson = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new AuthenticationDto(registration.email(), registration.password()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode loginResponse = objectMapper.readTree(loginJson);

        mockMvc.perform(get("/api/v1/orders/me")
                        .header("Authorization", "Bearer " + loginResponse.get("token").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void invalidCredentialsAndUnauthenticatedProtectedRequestAreRejected() throws Exception {
        String email = "registered-" + System.nanoTime() + "@test.com";
        userRepository.saveAndFlush(new User(null, "Known User", email,
                passwordEncoder.encode("correct-password"), Role.ROLE_USER));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new AuthenticationDto(email, "wrong-password"))))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/v1/orders/me"))
                .andExpect(status().is4xxClientError());
    }
}
