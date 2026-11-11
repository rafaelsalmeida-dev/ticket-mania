package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TicketBatchRequestDto(
        @NotNull TicketType type,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @Min(1) @Max(50_000) int quantity) {}