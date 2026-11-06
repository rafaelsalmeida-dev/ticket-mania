package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketBatchRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketUpdateRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.EventNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void generateTicketsUsesRepositoryBatchAndReturnsAvailableCount() {
        UUID eventId = UUID.randomUUID();
        TicketBatchRequestDto request = new TicketBatchRequestDto(
                TicketType.VIP, new BigDecimal("250.00"), 5);
        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(ticketRepository.countByEventIdAndTicketTypeAndStatus(
                eventId, TicketType.VIP, TicketStatus.AVAILABLE)).thenReturn(5L);

        var result = ticketService.generateTicketsForEvent(eventId, request);

        assertEquals(5, result.created());
        assertEquals(5L, result.totalAvailableOfType());
        verify(ticketRepository).generateBatch(eventId, request.price(), "VIP", 5);
    }

    @Test
    void generateTicketsRejectsUnknownEventWithoutWritingTickets() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.existsById(eventId)).thenReturn(false);

        assertThrows(EventNotFoundException.class, () -> ticketService.generateTicketsForEvent(
                eventId, new TicketBatchRequestDto(TicketType.VIP, BigDecimal.TEN, 2)));

        verify(ticketRepository, never()).generateBatch(
                eventId, BigDecimal.TEN, TicketType.VIP.name(), 2);
    }

    @Test
    void updatePriceChangesOnlyAvailableTicketsAndReturnsRemainingCount() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(ticketRepository.updatePriceOfAvailable(eventId, "FULL", new BigDecimal("75.00")))
                .thenReturn(2);
        when(ticketRepository.countByEventIdAndTicketTypeAndStatus(
                eventId, TicketType.FULL, TicketStatus.AVAILABLE)).thenReturn(3L);

        var result = ticketService.updatePrice(
                eventId, TicketType.FULL, new TicketUpdateRequestDto(new BigDecimal("75.00")));

        assertEquals(2, result.affected());
        assertEquals(3L, result.totalAvailableOfType());
    }
}
