package com.github.rafael_souza_de_almeida.ticket_mania.mapper;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderSummaryDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "id", target = "orderId")
    OrderSummaryDto toSummary(Order order);

    @Mapping(source = "id", target = "ticketId")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "event.name", target = "eventName")
    @Mapping(source = "ticketType", target = "ticketType")
    TicketItemDto toItem(Ticket ticket);
}
