package com.github.rafael_souza_de_almeida.ticket_mania.catalog.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.service.EventService;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketBatchRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketBatchResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketUpdateRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketUpdateResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;
    private final TicketService ticketService;

    public EventController(EventService eventService, TicketService ticketService) {
        this.eventService = eventService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<Page<EventResponseDto>> findAll(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "12") Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {

        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "name"));

        return ResponseEntity.ok(eventService.findAll(pageable));
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<Page<EventResponseDto>> findByName(@PathVariable String name,
                                                             @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                             @RequestParam(value = "size", defaultValue = "12") Integer size,
                                                             @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "name"));

        return ResponseEntity.ok(eventService.findByName(name, pageable));



    }

    @PostMapping
    public ResponseEntity<EventResponseDto> create(@RequestBody @Valid EventRequestDto requestDto) {

        EventResponseDto response = eventService.create(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> update(@PathVariable UUID id, @RequestBody @Valid EventRequestDto requestDto) {

        EventResponseDto response = eventService.update(requestDto, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {

        eventService.delete(id);

    }

    @PostMapping("/{eventId}/tickets")
    public ResponseEntity<TicketBatchResponseDto> generate(@PathVariable UUID eventId,
                                                           @RequestBody @Valid TicketBatchRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.generateTicketsForEvent(eventId, dto));
    }

    @PutMapping("/{eventId}/tickets/{type}")
    public TicketUpdateResponseDto updateTicketPrice(@PathVariable UUID eventId,
                                               @PathVariable TicketType type,
                                               @RequestBody @Valid TicketUpdateRequestDto dto) {
        return ticketService.updatePrice(eventId, type, dto);
    }

    @DeleteMapping("/{eventId}/tickets/{type}")
    public TicketUpdateResponseDto deleteTickets(@PathVariable UUID eventId,
                                          @PathVariable TicketType type,
                                          @RequestParam(required = false) @Min(1) Integer quantity) {
        return ticketService.deleteAvailable(eventId, type, quantity);
    }


}
