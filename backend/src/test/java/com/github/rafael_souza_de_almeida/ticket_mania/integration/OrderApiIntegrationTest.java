package com.github.rafael_souza_de_almeida.ticket_mania.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.core.security.TokenService;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import com.github.rafael_souza_de_almeida.ticket_mania.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderApiIntegrationTest extends AbstractIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Test
    void authenticatedPurchaseReservesTicketAndCanReadPersistedOrder() throws Exception {
        User buyer = userRepository.saveAndFlush(new User(
                null, "Buyer", "buyer-" + System.nanoTime() + "@test.com", "encoded-password", Role.ROLE_USER));
        Event event = eventRepository.saveAndFlush(new Event(
                null, "Integration event", LocalDateTime.now().plusDays(20), "Arena", 100, null));
        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setPrice(new BigDecimal("125.00"));
        ticket.setTicketType(TicketType.FULL);
        ticket.setStatus(TicketStatus.AVAILABLE);
        ticket = ticketRepository.saveAndFlush(ticket);
        String bearerToken = tokenService.generateToken(buyer);

        mockMvc.perform(get("/api/v1/orders/me"))
                .andExpect(status().is4xxClientError());

        String orderJson = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new OrderRequestDto(event.getId(), TicketType.FULL, 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(125.00))
                .andExpect(jsonPath("$.payment.provider").value("MOCK"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode createdOrder = objectMapper.readTree(orderJson);

        assertEquals(TicketStatus.RESERVED, ticketRepository.findById(ticket.getId()).orElseThrow().getStatus());
        var savedOrder = orderRepository.findById(
                java.util.UUID.fromString(createdOrder.get("orderId").asText())).orElseThrow();
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(buyer.getId(), savedOrder.getUserId());

        mockMvc.perform(get("/api/v1/orders/me")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
        assertEquals(1, orderRepository.findByUserId(buyer.getId(), PageRequest.of(0, 10)).getTotalElements());
    }
}
