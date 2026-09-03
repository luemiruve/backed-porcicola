package com.luemi.porcicola.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "montas")
public class Monta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_monta")
    private Integer idMonta;

    @ManyToOne
    @JoinColumn(name = "id_ciclo", nullable = false)
    private CicloReproductivo ciclo;

    @Column(name = "fecha_monta", nullable = false)
    private LocalDateTime fechaMonta;

    @Column(name = "es_inseminacion")
    private Boolean esInseminacion = false;

    @ManyToOne
    @JoinColumn(name = "id_semental")
    private Animal semental;

    public Integer getIdMonta() {
        return idMonta;
    }

    public void setIdMonta(Integer idMonta) {
        this.idMonta = idMonta;
    }

    public CicloReproductivo getCiclo() {
        return ciclo;
    }

    public void setCiclo(CicloReproductivo ciclo) {
        this.ciclo = ciclo;
    }

    public LocalDateTime getFechaMonta() {
        return fechaMonta;
    }

    public void setFechaMonta(LocalDateTime fechaMonta) {
        this.fechaMonta = fechaMonta;
    }

    public Boolean getEsInseminacion() {
        return esInseminacion;
    }

    public void setEsInseminacion(Boolean esInseminacion) {
        this.esInseminacion = esInseminacion;
    }

    public Animal getSemental() {
        return semental;
    }

    public void setSemental(Animal semental) {
        this.semental = semental;
    }
}

