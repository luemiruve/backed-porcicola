package com.luemi.porcicola.dto;

import com.luemi.porcicola.enums.CycleStatus;

import java.time.LocalDate;

public class ReproductiveCycleDTO {
    private Integer id;
    private Integer sowId;
    private Integer farrowingNumber;
    private LocalDate startDate;
    private LocalDate expectedFarrowingDate;
    private LocalDate actualFarrowingDate;
    private CycleStatus status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSowId() {
        return sowId;
    }

    public void setSowId(Integer sowId) {
        this.sowId = sowId;
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
}
