package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TicketBatchRequestDto(
        @NotNull int quantity,
        @NotNull TicketType type,
        @NotNull BigDecimal price
) {
}
