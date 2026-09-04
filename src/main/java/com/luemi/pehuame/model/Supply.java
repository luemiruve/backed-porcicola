package com.luemi.pehuame.model;

import com.luemi.pehuame.enums.SupplyUnit;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "supplies")
public class Supply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "current_quantity", nullable = false)
    private BigDecimal currentQuantity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "unit", nullable = false)
    private SupplyUnit unit;

    @Column(name = "estimated_daily_consumption")
    private BigDecimal estimatedDailyConsumption;

    @Column(name = "minimum_stock")
    private BigDecimal minimumStock;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "batch")
    private String batch;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "last_purchase_date")
    private LocalDate lastPurchaseDate;

    @PrePersist
    protected void onCreate() {
        if (this.lastPurchaseDate == null) {
            this.lastPurchaseDate = LocalDate.now();
        }
    }

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(BigDecimal currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public SupplyUnit getUnit() {
        return unit;
    }

    public void setUnit(SupplyUnit unit) {
        this.unit = unit;
    }

    public BigDecimal getEstimatedDailyConsumption() {
        return estimatedDailyConsumption;
    }

    public void setEstimatedDailyConsumption(BigDecimal estimatedDailyConsumption) {
        this.estimatedDailyConsumption = estimatedDailyConsumption;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(BigDecimal minimumStock) {
        this.minimumStock = minimumStock;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDate getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(LocalDate lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }
}

