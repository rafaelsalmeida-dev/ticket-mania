package com.github.rafael_souza_de_almeida.ticket_mania.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.core.security.TokenService;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import com.github.rafael_souza_de_almeida.ticket_mania.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventApiIntegrationTest extends AbstractIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Test
    void publicCatalogAndAdministratorEventCreationPersistAndReturnTheEvent() throws Exception {
        User administrator = saveUser(Role.ROLE_ADMIN, "admin");
        User regularUser = saveUser(Role.ROLE_USER, "user");
        String adminToken = tokenService.generateToken(administrator);
        String userToken = tokenService.generateToken(regularUser);
        EventRequestDto eventRequest = new EventRequestDto();
        eventRequest.setName("Live " + System.nanoTime());
        eventRequest.setPlace("Main Arena");
        eventRequest.setCapacity(12000);
        eventRequest.setDate(LocalDateTime.now().plusDays(30).withNano(0));
        eventRequest.setImageUrl("https://example.test/event.png");
        String eventJson = """
                {"name":"%s","date":"%s","place":"%s","capacity":%d,"imageUrl":"%s"}
                """.formatted(eventRequest.getName(), eventRequest.getDate(), eventRequest.getPlace(),
                eventRequest.getCapacity(), eventRequest.getImageUrl());

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        String createdJson = mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(eventRequest.getName()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode createdResponse = objectMapper.readTree(createdJson);
        Event persistedEvent = eventRepository.findById(
                java.util.UUID.fromString(createdResponse.get("id").asText())).orElseThrow();

        assertEquals(eventRequest.getPlace(), persistedEvent.getPlace());
        assertEquals(eventRequest.getCapacity(), persistedEvent.getCapacity());

        mockMvc.perform(get("/api/v1/events/search/{name}", eventRequest.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(persistedEvent.getId().toString()));

        mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCannotCreateEventWithNonPositiveCapacity() throws Exception {
        User administrator = saveUser(Role.ROLE_ADMIN, "admin");
        String token = tokenService.generateToken(administrator);
        String invalidEvent = """
                {"name":"Invalid capacity","date":"%s","place":"Arena","capacity":0}
                """.formatted(LocalDateTime.now().plusDays(10).withNano(0));

        mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEvent))
                .andExpect(status().isBadRequest());

        assertEquals(0, eventRepository.count());
    }

    private User saveUser(Role role, String prefix) {
        return userRepository.saveAndFlush(new User(
                null, prefix + " " + System.nanoTime(), prefix + "-" + System.nanoTime() + "@test.com",
                "encoded-password", role));
    }
}
