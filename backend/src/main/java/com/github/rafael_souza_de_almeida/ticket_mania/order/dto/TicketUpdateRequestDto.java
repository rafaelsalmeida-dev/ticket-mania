package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TicketUpdateRequestDto(
        @NotNull @DecimalMin("0.00") BigDecimal price) {}

