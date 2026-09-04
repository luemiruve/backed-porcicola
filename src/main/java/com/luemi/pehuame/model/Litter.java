package com.luemi.pehuame.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "litters")
public class Litter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cycle_id", nullable = false)
    private ReproductiveCycle cycle;

    @ManyToOne
    @JoinColumn(name = "sow_id", nullable = false)
    private Animal sow;

    @Column(name = "born_alive")
    private Integer bornAlive = 0;

    @Column(name = "born_dead")
    private Integer bornDead = 0;

    @Column(name = "mummified")
    private Integer mummified = 0;

    @Column(name = "males")
    private Integer males = 0;

    @Column(name = "females")
    private Integer females = 0;

    @Column(name = "avg_birth_weight")
    private BigDecimal avgBirthWeight;

    @Column(name = "avg_weaning_weight")
    private BigDecimal avgWeaningWeight;

    @Column(name = "scheduled_weaning_date")
    private LocalDate scheduledWeaningDate;

    @Column(name = "actual_weaning_date")
    private LocalDate actualWeaningDate;

    @Column(name = "notes")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

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

    public Animal getSow() {
        return sow;
    }

    public void setSow(Animal sow) {
        this.sow = sow;
    }

    public Integer getBornAlive() {
        return bornAlive;
    }

    public void setBornAlive(Integer bornAlive) {
        this.bornAlive = bornAlive;
    }

    public Integer getBornDead() {
        return bornDead;
    }

    public void setBornDead(Integer bornDead) {
        this.bornDead = bornDead;
    }

    public Integer getMummified() {
        return mummified;
    }

    public void setMummified(Integer mummified) {
        this.mummified = mummified;
    }

    public Integer getMales() {
        return males;
    }

    public void setMales(Integer males) {
        this.males = males;
    }

    public Integer getFemales() {
        return females;
    }

    public void setFemales(Integer females) {
        this.females = females;
    }

    public BigDecimal getAvgBirthWeight() {
        return avgBirthWeight;
    }

    public void setAvgBirthWeight(BigDecimal avgBirthWeight) {
        this.avgBirthWeight = avgBirthWeight;
    }

    public BigDecimal getAvgWeaningWeight() {
        return avgWeaningWeight;
    }

    public void setAvgWeaningWeight(BigDecimal avgWeaningWeight) {
        this.avgWeaningWeight = avgWeaningWeight;
    }

    public LocalDate getScheduledWeaningDate() {
        return scheduledWeaningDate;
    }

    public void setScheduledWeaningDate(LocalDate scheduledWeaningDate) {
        this.scheduledWeaningDate = scheduledWeaningDate;
    }

    public LocalDate getActualWeaningDate() {
        return actualWeaningDate;
    }

    public void setActualWeaningDate(LocalDate actualWeaningDate) {
        this.actualWeaningDate = actualWeaningDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }
}

