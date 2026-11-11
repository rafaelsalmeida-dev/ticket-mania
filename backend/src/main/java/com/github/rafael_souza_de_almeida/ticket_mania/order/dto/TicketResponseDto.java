package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;

import java.math.BigDecimal;

public record TicketResponseDto(
        TicketType ticketType,
        BigDecimal price,
        Long available
) {
}
