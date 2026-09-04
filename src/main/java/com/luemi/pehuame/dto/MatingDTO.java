package com.luemi.pehuame.dto;

import java.time.LocalDateTime;

public class MatingDTO {
    private Integer id;
    private Integer cycleId;
    private LocalDateTime matingDate;
    private Boolean isInsemination;
    private Integer boarId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCycleId() {
        return cycleId;
    }

    public void setCycleId(Integer cycleId) {
        this.cycleId = cycleId;
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

    public Integer getBoarId() {
        return boarId;
    }

    public void setBoarId(Integer boarId) {
        this.boarId = boarId;
    }
}
