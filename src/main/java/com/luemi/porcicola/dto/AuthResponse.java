package com.luemi.porcicola.dto;

public class AuthResponse {
    private String token;
    private String rol;
    private Integer idGranja;

    public AuthResponse (String token, String rol, Integer idGranja){
        this.token = token;
        this.rol = rol;
        this.idGranja = idGranja;
    }

    public String getToken() {
        return token;
    }

    public String getRol() {
        return rol;
    }

    public Integer getIdGranja() {
        return idGranja;
    }
}
