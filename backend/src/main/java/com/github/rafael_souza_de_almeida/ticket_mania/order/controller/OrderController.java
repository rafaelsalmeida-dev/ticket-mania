package com.github.rafael_souza_de_almeida.ticket_mania.order.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderSummaryDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.OrderPaymentService;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.OrderService;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<OrderResponseDto> create(@RequestBody @Valid OrderRequestDto requestDto,
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

    @GetMapping("/me")
    public ResponseEntity<Page<OrderSummaryDto>> findMyOrders(@AuthenticationPrincipal User user,
                                                             @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                             @RequestParam(value = "size", defaultValue = "12") Integer size,
                                                             @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "createdAt"));

        return ResponseEntity.status(HttpStatus.OK).body(orderService.findMyOrders(user.getId(), pageable));


    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderSummaryDto> findOrder(@AuthenticationPrincipal User user,
                                                           @PathVariable UUID orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findMyOrder(orderId, user.getId()));


    }

}
