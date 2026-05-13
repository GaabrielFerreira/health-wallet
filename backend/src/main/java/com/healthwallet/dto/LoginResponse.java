package com.healthwallet.dto;

import com.healthwallet.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private UUID id;
    private String name;
    private String email;
    private Role role;

    public LoginResponse(String token, UUID id, String name, String email, Role role) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
