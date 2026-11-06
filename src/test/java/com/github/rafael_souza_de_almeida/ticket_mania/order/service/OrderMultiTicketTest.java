package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.mapper.OrderMapper;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.dto.PaymentInit;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.gateway.PaymentGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMultiTicketTest {

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

    @Test
    void createSumsAndReservesAllTickets() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Ticket first = ticket(new BigDecimal("25.00"));
        Ticket second = ticket(new BigDecimal("40.00"));
        when(ticketRepository.findAvailable(eventId, "HALF", 2)).thenReturn(List.of(first, second));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });
        when(paymentGateway.start(any(Order.class))).thenReturn(new PaymentInit("MOCK", "reference"));

        var result = orderService.create(new OrderRequestDto(eventId, TicketType.HALF, 2), userId);

        assertEquals(new BigDecimal("65.00"), result.totalAmount());
        assertEquals(List.of(first.getId(), second.getId()), result.ticketIds());
        assertEquals(TicketStatus.RESERVED, first.getStatus());
        assertEquals(TicketStatus.RESERVED, second.getStatus());
        assertEquals(2, first.getOrder().getTickets().size());
        verify(paymentGateway).start(first.getOrder());
    }

    private Ticket ticket(BigDecimal price) {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setPrice(price);
        ticket.setTicketType(TicketType.HALF);
        ticket.setStatus(TicketStatus.AVAILABLE);
        return ticket;
    }
}
