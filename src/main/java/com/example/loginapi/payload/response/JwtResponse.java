package com.example.loginapi.payload.response;

import java.util.List;

public class JwtResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> cargos;

    public JwtResponse(Long id, String username, String email, List<String> cargos) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.cargos = cargos;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getCargo() {
        return cargos;
    }
}
