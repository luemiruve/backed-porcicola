package com.luemi.porcicola.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @ManyToOne
    @JoinColumn(name = "id_animal")
    private Animal animal;

    @ManyToOne
    @JoinColumn(name = "id_ciclo")
    private CicloReproductivo ciclo;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false)
    private String mensaje;

    @Column(name = "fecha_alerta", nullable = false)
    private LocalDate fechaAlerta;

    private Boolean leida = false;

    @Column(name = "tipo_alerta", length = 20)
    private String tipoAlerta;

    @ManyToOne
    @JoinColumn(name = "id_granja", nullable = false)
    private Granja granja;

    public Integer getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Integer idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }

    public CicloReproductivo getCiclo() {
        return ciclo;
    }

    public void setCiclo(CicloReproductivo ciclo) {
        this.ciclo = ciclo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDate getFechaAlerta() {
        return fechaAlerta;
    }

    public void setFechaAlerta(LocalDate fechaAlerta) {
        this.fechaAlerta = fechaAlerta;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public String getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(String tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    public Granja getGranja() {
        return granja;
    }

    public void setGranja(Granja granja) {
        this.granja = granja;
    }
}

