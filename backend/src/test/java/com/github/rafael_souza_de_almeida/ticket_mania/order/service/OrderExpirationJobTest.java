package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.gateway.PaymentGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderExpirationJobTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderExpirationJob orderExpirationJob;

    @Test
    void cancelExpiredOrdersExpiresOrdersAndReleasesReservedTickets() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now().minusMinutes(20));
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setStatus(TicketStatus.RESERVED);
        order.addTicket(ticket);
        when(orderRepository.findExpiredForUpdate(
                eq(OrderStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(order));

        orderExpirationJob.cancelExpiredOrders();

        assertEquals(OrderStatus.EXPIRED, order.getStatus());
        assertEquals(TicketStatus.AVAILABLE, ticket.getStatus());
        assertNull(ticket.getOrder());
        assertEquals(0, order.getTickets().size());
        verify(paymentGateway).cancel(order);
    }

    @Test
    void cancelExpiredOrdersDoesNotCancelWhenThereAreNoExpiredOrders() {
        when(orderRepository.findExpiredForUpdate(
                eq(OrderStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());

        orderExpirationJob.cancelExpiredOrders();

        verify(paymentGateway, never()).cancel(any(Order.class));
    }
}
