package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.gateway.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(OrderExpirationJob.class);
    private static final int BATCH_SIZE = 100;

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public OrderExpirationJob(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cancelExpiredOrders() {

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);

        List<Order> expired = orderRepository.findExpiredForUpdate(
                OrderStatus.PENDING, threshold, PageRequest.of(0, BATCH_SIZE));

        if (expired.isEmpty()) {
            return;
        }

        for (Order order : expired) {
            paymentGateway.cancel(order);
            order.setStatus(OrderStatus.EXPIRED);

            List<UUID> ticketIds = new ArrayList<>();
            order.getTickets().forEach(t -> {
                t.setStatus(TicketStatus.AVAILABLE);
                t.setOrder(null);
                ticketIds.add(t.getId());
            });
            order.getTickets().clear();

            log.info("Order {} expired. Tickets {} are AVAILABLE again.", order.getId(), ticketIds);
        }
    }
}
