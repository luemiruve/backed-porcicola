package com.luemi.porcicola.model;

import com.luemi.porcicola.enums.UnidadInsumo;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventario_insumos")
public class InventarioInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private Integer idInsumo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "cantidad_actual", nullable = false)
    private BigDecimal cantidadActual;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "unidad_insumo", nullable = false)
    private UnidadInsumo unidad;

    @Column(name = "consumo_diario_estimado")
    private BigDecimal consumoDiarioEstimado;

    @Column(name = "stock_minimo")
    private BigDecimal stockMinimo;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    private String proveedor;

    private String lote;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(name = "fecha_ultima_compra")
    private LocalDate fechaUltimaCompra;

    @PrePersist
    protected void onCreate() {
        if (this.fechaUltimaCompra == null) {
            this.fechaUltimaCompra = LocalDate.now();
        }
    }

    @ManyToOne
    @JoinColumn(name = "id_granja", nullable = false)
    private Granja granja;

    public Integer getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Integer idInsumo) {
        this.idInsumo = idInsumo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(BigDecimal cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public UnidadInsumo getUnidad() {
        return unidad;
    }

    public void setUnidad(UnidadInsumo unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getConsumoDiarioEstimado() {
        return consumoDiarioEstimado;
    }

    public void setConsumoDiarioEstimado(BigDecimal consumoDiarioEstimado) {
        this.consumoDiarioEstimado = consumoDiarioEstimado;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public LocalDate getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(LocalDate fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public LocalDate getFechaUltimaCompra() {
        return fechaUltimaCompra;
    }

    public void setFechaUltimaCompra(LocalDate fechaUltimaCompra) {
        this.fechaUltimaCompra = fechaUltimaCompra;
    }

    public Granja getGranja() {
        return granja;
    }

    public void setGranja(Granja granja) {
        this.granja = granja;
    }
}

