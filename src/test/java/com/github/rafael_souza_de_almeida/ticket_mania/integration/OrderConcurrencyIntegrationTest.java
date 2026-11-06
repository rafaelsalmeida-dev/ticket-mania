package com.github.rafael_souza_de_almeida.ticket_mania.integration;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.TicketUnavailableException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    private UUID eventId;
    private UUID ticketId;
    private final List<UUID> userIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        if (eventId == null) {
            return;
        }
        if (ticketId != null) {
            ticketRepository.deleteAllByIdInBatch(List.of(ticketId));
        }
        List<UUID> orderIds = orderRepository.findAll().stream()
                .filter(order -> userIds.contains(order.getUserId()))
                .map(order -> order.getId())
                .toList();
        orderRepository.deleteAllByIdInBatch(orderIds);
        eventRepository.deleteById(eventId);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentRequestsCannotReserveTheSameTicket() throws Exception {
        Event event = eventRepository.saveAndFlush(new Event(
                null, "Concurrency " + UUID.randomUUID(), LocalDateTime.now().plusDays(20), "Arena", 100, null));
        eventId = event.getId();
        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setPrice(new BigDecimal("100.00"));
        ticket.setTicketType(TicketType.VIP);
        ticket.setStatus(TicketStatus.AVAILABLE);
        ticketId = ticketRepository.saveAndFlush(ticket).getId();

        int callers = 8;
        ExecutorService executor = Executors.newFixedThreadPool(callers);
        CountDownLatch ready = new CountDownLatch(callers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int i = 0; i < callers; i++) {
                UUID userId = UUID.randomUUID();
                userIds.add(userId);
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        orderService.create(new OrderRequestDto(eventId, TicketType.VIP, 1), userId);
                        return true;
                    } catch (TicketUnavailableException unavailable) {
                        return false;
                    }
                }));
            }

            ready.await();
            start.countDown();
            int successfulOrders = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successfulOrders++;
                }
            }

            assertEquals(1, successfulOrders);
            assertEquals(1, orderRepository.count());
            assertEquals(TicketStatus.RESERVED, ticketRepository.findById(ticketId).orElseThrow().getStatus());
        } finally {
            executor.shutdownNow();
        }
    }
}
