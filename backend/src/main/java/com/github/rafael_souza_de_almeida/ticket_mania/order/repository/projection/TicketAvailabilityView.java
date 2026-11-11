package com.github.rafael_souza_de_almeida.ticket_mania.order.repository.projection;

import java.math.BigDecimal;

public interface TicketAvailabilityView {
    String getTicketType();
    BigDecimal getPrice();
    Long getAvailable();
}
