package com.luemi.porcicola.model;

import com.luemi.porcicola.enums.CycleStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "reproductive_cycles")
@DynamicUpdate
public class ReproductiveCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sow_id", nullable = false)
    private Animal sow;

    @Column(name = "farrowing_number", nullable = false)
    private Integer farrowingNumber;

    @Column(name = "start_date")
    private LocalDate startDate;

    @PrePersist
    protected void onCreate() {
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }

    @Column(name = "expected_farrowing_date")
    private LocalDate expectedFarrowingDate;

    @Column(name = "actual_farrowing_date")
    private LocalDate actualFarrowingDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private CycleStatus status = CycleStatus.GESTATION;

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Animal getSow() {
        return sow;
    }

    public void setSow(Animal sow) {
        this.sow = sow;
    }

    public Integer getFarrowingNumber() {
        return farrowingNumber;
    }

    public void setFarrowingNumber(Integer farrowingNumber) {
        this.farrowingNumber = farrowingNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpectedFarrowingDate() {
        return expectedFarrowingDate;
    }

    public void setExpectedFarrowingDate(LocalDate expectedFarrowingDate) {
        this.expectedFarrowingDate = expectedFarrowingDate;
    }

    public LocalDate getActualFarrowingDate() {
        return actualFarrowingDate;
    }

    public void setActualFarrowingDate(LocalDate actualFarrowingDate) {
        this.actualFarrowingDate = actualFarrowingDate;
    }

    public CycleStatus getStatus() {
        return status;
    }

    public void setStatus(CycleStatus status) {
        this.status = status;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }
}

