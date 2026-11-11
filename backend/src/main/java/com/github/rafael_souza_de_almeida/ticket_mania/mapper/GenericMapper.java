package com.github.rafael_souza_de_almeida.ticket_mania.mapper;

import java.util.List;

public interface GenericMapper<E, REQ, RESP> {

    RESP toResponseDto(E entity);
    E toEntity(REQ dto);

    List<RESP> toResponseDtoList(List<E> entityList);


}
