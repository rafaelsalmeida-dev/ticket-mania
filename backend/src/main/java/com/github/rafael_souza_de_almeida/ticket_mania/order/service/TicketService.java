package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketBatchRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketBatchResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketUpdateRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketUpdateResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.EventNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public TicketService(TicketRepository ticketRepository, EventRepository eventRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public TicketBatchResponseDto generateTicketsForEvent(UUID eventId, TicketBatchRequestDto dto) {

        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("Event not found");
        }

        ticketRepository.generateBatch(
                eventId,
                dto.price(),
                dto.type().name(),
                dto.quantity());

        long total = ticketRepository.countByEventIdAndTicketTypeAndStatus(
                eventId, dto.type(), TicketStatus.AVAILABLE);

        return new TicketBatchResponseDto(dto.quantity(), total);
    }

    @Transactional
    public TicketUpdateResponseDto updatePrice(UUID eventId, TicketType type, TicketUpdateRequestDto dto) {
        checkEventExists(eventId);

        int updated = ticketRepository.updatePriceOfAvailable(eventId, type.name(), dto.price());

        return new TicketUpdateResponseDto(updated, countAvailable(eventId, type));
    }


    @Transactional
    public TicketUpdateResponseDto deleteAvailable(UUID eventId, TicketType type, Integer quantity) {
        checkEventExists(eventId);

        int limit = (quantity == null) ? Integer.MAX_VALUE : quantity;
        int deleted = ticketRepository.deleteAvailable(eventId, type.name(), limit);

        return new TicketUpdateResponseDto(deleted, countAvailable(eventId, type));
    }

    private void checkEventExists(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("Event not found");
        }
    }

    private long countAvailable(UUID eventId, TicketType type) {
        return ticketRepository.countByEventIdAndTicketTypeAndStatus(eventId, type, TicketStatus.AVAILABLE);
    }
}

