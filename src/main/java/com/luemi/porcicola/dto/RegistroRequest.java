package com.luemi.porcicola.dto;

public class RegistroRequest {
    private String nombre;
    private String email;
    private String password;
    private String telefono;
    private String nombreGranja;
    private String ubicacionGranja;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombreGranja() {
        return nombreGranja;
    }

    public void setNombreGranja(String nombreGranja) {
        this.nombreGranja = nombreGranja;
    }

    public String getUbicacionGranja() {
        return ubicacionGranja;
    }

    public void setUbicacionGranja(String ubicacionGranja) {
        this.ubicacionGranja = ubicacionGranja;
    }
}
