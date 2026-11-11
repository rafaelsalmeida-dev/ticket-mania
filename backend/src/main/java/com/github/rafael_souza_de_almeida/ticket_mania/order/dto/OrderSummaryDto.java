package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderSummaryDto(
        UUID orderId,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<TicketItemDto> tickets)
{

}
