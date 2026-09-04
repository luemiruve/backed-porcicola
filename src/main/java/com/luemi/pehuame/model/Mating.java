package com.luemi.pehuame.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matings")
public class Mating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReproductiveCycle cycle;

    @Column(name = "mating_date", nullable = false)
    private LocalDateTime matingDate;

    @Column(name = "is_insemination")
    private Boolean isInsemination = false;

    @ManyToOne
    @JoinColumn(name = "boar_id")
    private Animal boar;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ReproductiveCycle getCycle() {
        return cycle;
    }

    public void setCycle(ReproductiveCycle cycle) {
        this.cycle = cycle;
    }

    public LocalDateTime getMatingDate() {
        return matingDate;
    }

    public void setMatingDate(LocalDateTime matingDate) {
        this.matingDate = matingDate;
    }

    public Boolean getIsInsemination() {
        return isInsemination;
    }

    public void setIsInsemination(Boolean isInsemination) {
        this.isInsemination = isInsemination;
    }

    public Animal getBoar() {
        return boar;
    }

    public void setBoar(Animal boar) {
        this.boar = boar;
    }
}

