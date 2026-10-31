package com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class EventRequestDto {

    @NotBlank(message = "Event must have a name.")
    private String name;

    @NotNull(message = "Event must have a date.")
    @Future(message = "Event date must be in the future.")
    private LocalDateTime date;

    @NotBlank(message = "Event must have a place.")
    private String place;

    @NotNull(message = "Event must have a capacity value.")
    @Positive(message = "The capacity must be greater than zero.")
    private Integer capacity;

    public EventRequestDto() {
    }

    public @NotBlank(message = "Event must have a name.") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Event must have a name.") String name) {
        this.name = name;
    }

    public @NotNull(message = "Event must have a date.") @Future(message = "Event date must be in the future.") LocalDateTime getDate() {
        return date;
    }

    public void setDate(@NotNull(message = "Event must have a date.") @Future(message = "Event date must be in the future.") LocalDateTime date) {
        this.date = date;
    }

    public @NotBlank(message = "Event must have a place.") String getPlace() {
        return place;
    }

    public void setPlace(@NotBlank(message = "Event must have a place.") String place) {
        this.place = place;
    }

    public @NotNull(message = "Event must have a capacity value.") @Positive(message = "The capacity must be greater than zero.") Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(@NotNull(message = "Event must have a capacity value.") @Positive(message = "The capacity must be greater than zero.") Integer capacity) {
        this.capacity = capacity;
    }
}
