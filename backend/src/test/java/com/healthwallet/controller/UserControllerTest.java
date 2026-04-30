package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.RegisterRequest;
import com.healthwallet.dto.RegisterResponse;
import com.healthwallet.exception.EmailAlreadyExistsException;
import com.healthwallet.model.Role;
import com.healthwallet.security.SecurityConfig;
import com.healthwallet.security.UserDetailsServiceImpl;
import com.healthwallet.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void register_returns201_whenRequestIsValid() throws Exception {
        RegisterRequest request = buildRequest("João", "joao@email.com", "12345678901", "senha123", Role.PATIENT);
        RegisterResponse response = new RegisterResponse(UUID.randomUUID(), "João", "joao@email.com");

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void register_returns400_whenNameIsBlank() throws Exception {
        RegisterRequest request = buildRequest("", "joao@email.com", "12345678901", "senha123", Role.PATIENT);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.name").exists());
    }

    @Test
    void register_returns400_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = buildRequest("João", "nao-e-email", "12345678901", "senha123", Role.PATIENT);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void register_returns400_whenCpfIsInvalid() throws Exception {
        RegisterRequest request = buildRequest("João", "joao@email.com", "123", "senha123", Role.PATIENT);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").exists());
    }

    @Test
    void register_returns400_whenPasswordIsTooShort() throws Exception {
        RegisterRequest request = buildRequest("João", "joao@email.com", "12345678901", "123", Role.PATIENT);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.password").exists());
    }

    @Test
    void register_returns409_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = buildRequest("João", "joao@email.com", "12345678901", "senha123", Role.PATIENT);

        when(authService.register(any())).thenThrow(new EmailAlreadyExistsException("joao@email.com"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void register_returns400_whenRoleIsNull() throws Exception {
        RegisterRequest request = buildRequest("João", "joao@email.com", "12345678901", "senha123", null);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.role").exists());
    }

    private RegisterRequest buildRequest(String name, String email, String cpf, String password, Role role) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setCpf(cpf);
        request.setPassword(password);
        request.setRole(role);
        return request;
    }
}
