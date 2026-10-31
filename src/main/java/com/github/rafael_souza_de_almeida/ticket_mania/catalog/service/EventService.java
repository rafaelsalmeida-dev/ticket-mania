package com.github.rafael_souza_de_almeida.ticket_mania.catalog.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.mapper.EventMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService{

    private final EventRepository eventRepository;
    private final EventMapper mapper;

    public EventService(EventRepository eventRepository, EventMapper mapper) {
        this.eventRepository = eventRepository;
        this.mapper = mapper;
    }

    public List<EventResponseDto> findAll () {

        var events = eventRepository.findAll();

        return mapper.toResponseDtoList(events);

    }

    public EventResponseDto create(EventRequestDto dto) {

        Event event = new Event();

        event.setName(dto.getName());
        event.setCapacity(dto.getCapacity());
        event.setDate(dto.getDate());
        event.setPlace(dto.getPlace());

        eventRepository.save(event);

        return mapper.toResponseDto(event);

    }

}
