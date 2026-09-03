package com.luemi.porcicola.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne
    @JoinColumn(name = "cycle_id")
    private ReproductiveCycle cycle;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(name = "alert_date", nullable = false)
    private LocalDate alertDate;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "alert_type", length = 20)
    private String alertType;

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

    public ReproductiveCycle getCycle() {
        return cycle;
    }

    public void setCycle(ReproductiveCycle cycle) {
        this.cycle = cycle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getAlertDate() {
        return alertDate;
    }

    public void setAlertDate(LocalDate alertDate) {
        this.alertDate = alertDate;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }
}

