package com.github.rafael_souza_de_almeida.ticket_mania.catalog.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.mapper.EventMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    private Event event;
    private EventResponseDto response;

    @BeforeEach
    void setUp() {
        event = new Event(UUID.randomUUID(), "Flamengo x São Paulo",
                LocalDateTime.now().plusMonths(6), "Maracanã", 65000, null);
        response = new EventResponseDto();
        response.setId(event.getId());
        response.setName(event.getName());
        response.setDate(event.getDate());
        response.setPlace(event.getPlace());
        response.setCapacity(event.getCapacity());
    }

    @Test
    void findAllMapsEventsToResponses() {
        when(eventRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(eventMapper.toResponseDto(event)).thenReturn(response);

        var result = eventService.findAll(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals(response, result.getContent().get(0));
    }

    @Test
    void findAllReturnsEmptyPageWhenThereAreNoEvents() {
        when(eventRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = eventService.findAll(Pageable.unpaged());

        assertTrue(result.isEmpty());
    }

    @Test
    void createPersistsAndMapsRequestedEvent() {
        EventRequestDto request = new EventRequestDto();
        request.setName(event.getName());
        request.setDate(event.getDate());
        request.setPlace(event.getPlace());
        request.setCapacity(event.getCapacity());

        when(eventMapper.toResponseDto(any(Event.class))).thenReturn(response);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        EventResponseDto result = eventService.create(request);

        verify(eventRepository).save(eventCaptor.capture());
        assertEquals(request.getName(), eventCaptor.getValue().getName());
        assertEquals(request.getDate(), eventCaptor.getValue().getDate());
        assertEquals(request.getPlace(), eventCaptor.getValue().getPlace());
        assertEquals(request.getCapacity(), eventCaptor.getValue().getCapacity());
        assertEquals(response, result);
    }
}
