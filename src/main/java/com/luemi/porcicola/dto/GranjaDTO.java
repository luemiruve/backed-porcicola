package com.luemi.porcicola.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

// dto/GranjaDTO.java
public class GranjaDTO {
    private Integer idGranja;
    private String nombre;
    private String ubicacion;
    private LocalDateTime fechaCreacion;

    // getters y setters

    public Integer getIdGranja() {
        return idGranja;
    }

    public void setIdGranja(Integer idGranja) {
        this.idGranja = idGranja;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}