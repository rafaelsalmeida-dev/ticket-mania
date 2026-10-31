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
import com.stripe.exception.StripeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderTransactionalService {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final PaymentService paymentService;

    public OrderTransactionalService(OrderRepository orderRepository, TicketRepository ticketRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public OrderResponseDto createWithTransaction(OrderRequestDto dto, UUID userId) {

        Ticket ticket = ticketRepository.findByIdAndStatus(dto.ticketId(), TicketStatus.AVAILABLE)
                .orElseThrow(() -> new TicketUnavailableException("Unavailable ticket."));


        ticket.setStatus(TicketStatus.RESERVED);
        ticketRepository.save(ticket);


        Order order = new Order();

        order.setTicket(ticket);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        try {
            String clientSecret = paymentService.createPaymentIntent(savedOrder);

            return new OrderResponseDto(savedOrder.getId(),
                    savedOrder.getTicket().getId(),
                    savedOrder.getUserId(),
                    clientSecret);

        } catch (StripeException e) {
            throw new RuntimeException("Failed to initialize payment with Stripe", e);
        }

    }
}
