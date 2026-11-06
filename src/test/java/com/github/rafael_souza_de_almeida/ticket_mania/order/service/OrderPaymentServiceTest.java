package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.core.rabbitMq.RabbitMQConfig;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.OrderNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderPaymentService orderPaymentService;

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void fulfillOrderMarksTicketsSoldAndPublishesOnlyAfterCommit() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Order order = pendingOrder(orderId, userId);
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVED);
        order.addTicket(ticket);
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        TransactionSynchronizationManager.initSynchronization();

        orderPaymentService.fulfillOrder(orderId, userId);

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(TicketStatus.SOLD, ticket.getStatus());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, orderId.toString());
    }

    @Test
    void fulfillOrderRejectsAnOrderOwnedByAnotherUser() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdForUpdate(orderId))
                .thenReturn(Optional.of(pendingOrder(orderId, UUID.randomUUID())));

        assertThrows(AccessDeniedException.class,
                () -> orderPaymentService.fulfillOrder(orderId, UUID.randomUUID()));

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void fulfillOrderRejectsUnknownOrder() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderPaymentService.fulfillOrder(orderId, UUID.randomUUID()));

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    private Order pendingOrder(UUID orderId, UUID userId) {
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }
}
