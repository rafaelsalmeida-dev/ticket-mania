package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderRequestDto(
        @NotNull UUID eventId,
        @NotNull TicketType ticketType,
        @Min(1) @Max(10) int quantity) {}
