package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.mapper.OrderMapper;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.TicketUnavailableException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.dto.PaymentInit;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.gateway.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private UUID eventId;
    private UUID userId;
    private Ticket ticket;
    private OrderRequestDto request;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        userId = UUID.randomUUID();
        ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setPrice(new BigDecimal("45.50"));
        ticket.setTicketType(TicketType.FULL);
        ticket.setStatus(TicketStatus.AVAILABLE);
        request = new OrderRequestDto(eventId, TicketType.FULL, 1);
    }

    @Test
    void createReservesRequestedTicketsAndStartsPayment() {
        UUID orderId = UUID.randomUUID();
        when(ticketRepository.findAvailable(eventId, "FULL", 1)).thenReturn(List.of(ticket));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });
        when(paymentGateway.start(any(Order.class))).thenReturn(new PaymentInit("MOCK", "mock_" + orderId));

        var result = orderService.create(request, userId);

        assertEquals(orderId, result.orderId());
        assertEquals(List.of(ticket.getId()), result.ticketIds());
        assertEquals(new BigDecimal("45.50"), result.totalAmount());
        assertEquals(OrderStatus.PENDING, ticket.getOrder().getStatus());
        assertEquals(TicketStatus.RESERVED, ticket.getStatus());
        assertEquals("MOCK", result.payment().provider());
        assertEquals("mock_" + orderId, result.payment().reference());
        verify(orderRepository).save(any(Order.class));
        verify(paymentGateway).start(ticket.getOrder());
    }

    @Test
    void createRejectsWhenTheRequestedQuantityIsUnavailable() {
        when(ticketRepository.findAvailable(eventId, "FULL", 1)).thenReturn(List.of());

        assertThrows(TicketUnavailableException.class, () -> orderService.create(request, userId));

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentGateway, never()).start(any(Order.class));
    }
}
