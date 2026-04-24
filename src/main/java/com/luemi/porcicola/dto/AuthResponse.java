package com.luemi.porcicola.dto;

public class AuthResponse {
    private String token;
    private String role;
    private Integer farmId;

    public AuthResponse(String token, String role, Integer farmId) {
        this.token = token;
        this.role = role;
        this.farmId = farmId;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public Integer getFarmId() {
        return farmId;
    }
}
