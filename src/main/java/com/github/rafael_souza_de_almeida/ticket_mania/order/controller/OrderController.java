package com.github.rafael_souza_de_almeida.ticket_mania.order.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.OrderPaymentService;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.OrderService;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")

public class OrderController {

    private final OrderService orderService;
    private final OrderPaymentService orderPaymentService;

    public OrderController(OrderService orderService, OrderPaymentService orderPaymentService) {
        this.orderService = orderService;
        this.orderPaymentService = orderPaymentService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> create(@RequestBody OrderRequestDto requestDto,
                                                   @AuthenticationPrincipal User user) {

        OrderResponseDto response = orderService.create(requestDto, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PatchMapping("/mock-success/{orderId}")
    public ResponseEntity<Void> simulateSuccessfulPayment(@PathVariable UUID orderId,
                                                          @AuthenticationPrincipal User user) {
        orderPaymentService.fulfillOrder(orderId, user.getId());
        return ResponseEntity.ok().build();
    }

}
