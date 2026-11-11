package com.github.rafael_souza_de_almeida.ticket_mania.mapper;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper extends GenericMapper<Event, EventRequestDto, EventResponseDto> {

    @Override
    @Mapping(target = "id", ignore = true)
    Event toEntity(EventRequestDto dto);
}
