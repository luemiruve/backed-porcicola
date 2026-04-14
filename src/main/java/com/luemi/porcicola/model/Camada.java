package com.luemi.porcicola.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "camadas")
public class Camada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_camada")
    private Integer idCamada;

    @ManyToOne
    @JoinColumn(name = "id_ciclo", nullable = false)
    private CicloReproductivo ciclo;

    @ManyToOne
    @JoinColumn(name = "id_madre", nullable = false)
    private Animal madre;

    @Column(name = "nacidos_vivos")
    private Integer nacidosVivos = 0;

    @Column(name = "nacidos_muertos")
    private Integer nacidosMuertos = 0;

    private Integer momias = 0;

    private Integer machos = 0;

    private Integer hembras = 0;

    @Column(name = "peso_nacimiento_prom")
    private BigDecimal pesoNacimientoProm;

    @Column(name = "peso_destete_prom")
    private BigDecimal pesoDesteteProm;

    @Column(name = "fecha_destete_programada")
    private LocalDate fechaDesteteProgramada;

    @Column(name = "fecha_destete_real")
    private LocalDate fechaDesteteReal;

    private String notas;

    @ManyToOne
    @JoinColumn(name = "id_granja", nullable = false)
    private Granja granja;

    public Integer getIdCamada() {
        return idCamada;
    }

    public void setIdCamada(Integer idCamada) {
        this.idCamada = idCamada;
    }

    public CicloReproductivo getCiclo() {
        return ciclo;
    }

    public void setCiclo(CicloReproductivo ciclo) {
        this.ciclo = ciclo;
    }

    public Animal getMadre() {
        return madre;
    }

    public void setMadre(Animal madre) {
        this.madre = madre;
    }

    public Integer getNacidosVivos() {
        return nacidosVivos;
    }

    public void setNacidosVivos(Integer nacidosVivos) {
        this.nacidosVivos = nacidosVivos;
    }

    public Integer getNacidosMuertos() {
        return nacidosMuertos;
    }

    public void setNacidosMuertos(Integer nacidosMuertos) {
        this.nacidosMuertos = nacidosMuertos;
    }

    public Integer getMomias() {
        return momias;
    }

    public void setMomias(Integer momias) {
        this.momias = momias;
    }

    public Integer getMachos() {
        return machos;
    }

    public void setMachos(Integer machos) {
        this.machos = machos;
    }

    public Integer getHembras() {
        return hembras;
    }

    public void setHembras(Integer hembras) {
        this.hembras = hembras;
    }

    public BigDecimal getPesoNacimientoProm() {
        return pesoNacimientoProm;
    }

    public void setPesoNacimientoProm(BigDecimal pesoNacimientoProm) {
        this.pesoNacimientoProm = pesoNacimientoProm;
    }

    public BigDecimal getPesoDesteteProm() {
        return pesoDesteteProm;
    }

    public void setPesoDesteteProm(BigDecimal pesoDesteteProm) {
        this.pesoDesteteProm = pesoDesteteProm;
    }

    public LocalDate getFechaDesteteProgramada() {
        return fechaDesteteProgramada;
    }

    public void setFechaDesteteProgramada(LocalDate fechaDesteteProgramada) {
        this.fechaDesteteProgramada = fechaDesteteProgramada;
    }

    public LocalDate getFechaDesteteReal() {
        return fechaDesteteReal;
    }

    public void setFechaDesteteReal(LocalDate fechaDesteteReal) {
        this.fechaDesteteReal = fechaDesteteReal;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Granja getGranja() {
        return granja;
    }

    public void setGranja(Granja granja) {
        this.granja = granja;
    }
}

