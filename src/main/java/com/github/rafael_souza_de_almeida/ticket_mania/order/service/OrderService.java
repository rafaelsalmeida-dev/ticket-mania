package com.github.rafael_souza_de_almeida.ticket_mania.order.service;


import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.TicketUnavailableException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.dto.PaymentInit;
import com.github.rafael_souza_de_almeida.ticket_mania.payment.gateway.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final PaymentGateway paymentGateway;

    public OrderService(OrderRepository orderRepository, TicketRepository ticketRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public OrderResponseDto create(OrderRequestDto dto, UUID userId) {

        List<Ticket> tickets = ticketRepository.findAvailable(dto.eventId(),
                dto.ticketType().toString(),
                dto.quantity());

        if(tickets.size() < dto.quantity()) {
            throw new TicketUnavailableException("Not enough tickets available");
        }

        BigDecimal total = tickets.stream().map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        tickets.forEach(t -> {
            t.setStatus(TicketStatus.RESERVED);
            saved.addTicket(t);
        });

        PaymentInit payment = paymentGateway.start(saved);
        saved.setPaymentProvider(payment.provider());
        saved.setPaymentReference(payment.reference());

        return new OrderResponseDto(saved.getId(), tickets.stream().map(Ticket::getId).toList(),
                total,
                payment);

    }



}
