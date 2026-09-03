package com.luemi.porcicola.dto;

public class UserDTO {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Boolean active;
    private Integer farmId;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getFarmId() {
        return farmId;
    }

    public void setFarmId(Integer farmId) {
        this.farmId = farmId;
    }
}
