package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderExpirationJob {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    public OrderExpirationJob(OrderRepository orderRepository, TicketRepository ticketRepository) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
    }

    Logger log = LoggerFactory.getLogger(OrderExpirationJob.class);

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredOrders() {

        LocalDateTime expiredThreshold = LocalDateTime.now().minusMinutes(15);

        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, expiredThreshold);

        if(expiredOrders.isEmpty()) {
            return;
        }

        log.info("Found {} expired orders. Starting cancellation process...", expiredOrders.size());

        for(Order order : expiredOrders) {

            order.setStatus(OrderStatus.CANCELLED);

            Ticket ticket = order.getTicket();

            ticket.setStatus(TicketStatus.AVAILABLE);

            ticketRepository.save(ticket);
            orderRepository.save(order);

            log.info("Order {} cancelled. Ticket {} is AVAILABLE again.", order.getId(), ticket.getId());

        }

    }

}
