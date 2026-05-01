package com.healthwallet.dto;

import com.healthwallet.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private String name;
    private String email;
    private Role role;

    public LoginResponse(String token, String name, String email, Role role) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
