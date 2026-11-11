package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import com.github.rafael_souza_de_almeida.ticket_mania.payment.dto.PaymentInit;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(UUID orderId,
                               List<UUID> ticketIds,
                               BigDecimal totalAmount,
                               PaymentInit payment) {}
