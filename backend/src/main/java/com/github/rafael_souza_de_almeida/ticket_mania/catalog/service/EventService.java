package com.github.rafael_souza_de_almeida.ticket_mania.catalog.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.mapper.EventMapper;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.EventNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventService{

    private final EventRepository eventRepository;
    private final EventMapper mapper;

    public EventService(EventRepository eventRepository, EventMapper mapper) {
        this.eventRepository = eventRepository;
        this.mapper = mapper;
    }

    public Page<EventResponseDto> findAll (Pageable pageable) {

        var events = eventRepository.findAll(pageable);

        return events.map(mapper::toResponseDto);

    }

    public Page<EventResponseDto> findByName(String name, Pageable pageable) {

        Page<Event> events = eventRepository.findByNameContainingIgnoreCase(name, pageable);

        return events.map(mapper::toResponseDto);

    }

    public EventResponseDto create(EventRequestDto dto) {

        Event event = new Event();

        event.setName(dto.getName());
        event.setCapacity(dto.getCapacity());
        event.setDate(dto.getDate());
        event.setPlace(dto.getPlace());
        event.setImageUrl(dto.getImageUrl());

        eventRepository.save(event);

        return mapper.toResponseDto(event);

    }

    public EventResponseDto update(EventRequestDto dto, UUID id) {

        Event event = findById(id);

        event.setName(dto.getName());
        event.setPlace(dto.getPlace());
        event.setDate(dto.getDate());
        event.setCapacity(dto.getCapacity());
        event.setImageUrl(dto.getImageUrl());

        Event updatedEvent = eventRepository.save(event);

        return mapper.toResponseDto(updatedEvent);

    }

    public void delete(UUID id) {

        Event event = findById(id);

        eventRepository.delete(event);

    }

    private Event findById(UUID id) {

        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
    }

}
