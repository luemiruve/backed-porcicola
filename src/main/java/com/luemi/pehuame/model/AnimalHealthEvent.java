package com.luemi.pehuame.model;

import com.luemi.pehuame.enums.EventType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "animal_health_events")
public class AnimalHealthEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "supply_id")
    private Supply supply;

    @Column(name = "dose")
    private String dose;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @PrePersist
    protected void onCreate() {
        if (this.eventDate == null) {
            this.eventDate = LocalDateTime.now();
        }
    }

    @Column(name = "next_dose")
    private LocalDate nextDose;

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Supply getSupply() {
        return supply;
    }

    public void setSupply(Supply supply) {
        this.supply = supply;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public LocalDate getNextDose() {
        return nextDose;
    }

    public void setNextDose(LocalDate nextDose) {
        this.nextDose = nextDose;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }
}

