package com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String place;

    @Column(nullable = false)
    private Integer capacity;

    public Event(UUID id, String name, LocalDateTime date, String place, Integer capacity) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.place = place;
        this.capacity = capacity;
    }

    public Event() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event event)) return false;
        return Objects.equals(getId(), event.getId()) && Objects.equals(getName(), event.getName()) && Objects.equals(getDate(), event.getDate()) && Objects.equals(getPlace(), event.getPlace()) && Objects.equals(getCapacity(), event.getCapacity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDate(), getPlace(), getCapacity());
    }
}
