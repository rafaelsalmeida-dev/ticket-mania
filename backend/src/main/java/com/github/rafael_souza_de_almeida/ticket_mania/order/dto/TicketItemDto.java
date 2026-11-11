package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketItemDto(
        UUID ticketId,
        UUID eventId,
        String eventName,
        TicketType ticketType,
        BigDecimal price
) {
}
