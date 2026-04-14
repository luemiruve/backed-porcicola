package com.luemi.porcicola.model;

import com.luemi.porcicola.enums.EstadoCiclo;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "ciclos_reproductivos")
public class CicloReproductivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ciclo")
    private Integer idCiclo;

    @ManyToOne
    @JoinColumn(name = "id_madre", nullable = false)
    private Animal madre;

    @Column(name = "numero_parto", nullable = false)
    private Integer numeroParto;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @PrePersist
    protected void onCreate() {
        if (this.fechaInicio == null) {
            this.fechaInicio = LocalDate.now();
        }
    }

    @Column(name = "fecha_probable_parto")
    private LocalDate fechaProbableParto;

    @Column(name = "fecha_parto_real")
    private LocalDate fechaPartoReal;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado")
    private EstadoCiclo estado = EstadoCiclo.Gestacion;

    @ManyToOne
    @JoinColumn(name = "id_granja", nullable = false)
    private Granja granja;

    public Integer getIdCiclo() {
        return idCiclo;
    }

    public void setIdCiclo(Integer idCiclo) {
        this.idCiclo = idCiclo;
    }

    public Animal getMadre() {
        return madre;
    }

    public void setMadre(Animal madre) {
        this.madre = madre;
    }

    public Integer getNumeroParto() {
        return numeroParto;
    }

    public void setNumeroParto(Integer numeroParto) {
        this.numeroParto = numeroParto;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaProbableParto() {
        return fechaProbableParto;
    }

    public void setFechaProbableParto(LocalDate fechaProbableParto) {
        this.fechaProbableParto = fechaProbableParto;
    }

    public LocalDate getFechaPartoReal() {
        return fechaPartoReal;
    }

    public void setFechaPartoReal(LocalDate fechaPartoReal) {
        this.fechaPartoReal = fechaPartoReal;
    }



    public EstadoCiclo getEstado() {
        return estado;
    }

    public void setEstado(EstadoCiclo estado) {
        this.estado = estado;
    }

    public Granja getGranja() {
        return granja;
    }

    public void setGranja(Granja granja) {
        this.granja = granja;
    }
}

