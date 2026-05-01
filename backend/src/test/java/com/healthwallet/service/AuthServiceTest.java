package com.healthwallet.service;

import com.healthwallet.dto.LoginRequest;
import com.healthwallet.dto.LoginResponse;
import com.healthwallet.dto.RegisterRequest;
import com.healthwallet.dto.RegisterResponse;
import com.healthwallet.exception.EmailAlreadyExistsException;
import com.healthwallet.model.Role;
import com.healthwallet.model.User;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_returnsResponse_whenEmailIsNew() {
        RegisterRequest request = buildRegisterRequest("João", "joao@email.com", "12345678901", "senha123", Role.PATIENT);

        User saved = new User();
        saved.setName(request.getName());
        saved.setEmail(request.getEmail());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        RegisterResponse response = authService.register(request);

        assertThat(response.getName()).isEqualTo("João");
        assertThat(response.getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    void register_encodesPassword_beforeSaving() {
        RegisterRequest request = buildRegisterRequest("João", "joao@email.com", "12345678901", "senha123", Role.PATIENT);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
    }

    @Test
    void register_throwsEmailAlreadyExists_whenEmailIsTaken() {
        RegisterRequest request = buildRegisterRequest("João", "joao@email.com", "12345678901", "senha123", Role.PATIENT);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("joao@email.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_setsCorrectRole() {
        RegisterRequest request = buildRegisterRequest("Dra. Ana", "ana@email.com", "98765432100", "senha123", Role.DOCTOR);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.DOCTOR);
    }

    @Test
    void login_returnsTokenAndUserInfo_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@email.com");
        request.setPassword("senha123");

        User user = new User();
        user.setName("João");
        user.setEmail("joao@email.com");
        user.setRole(Role.PATIENT);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "joao@email.com", "hashed", List.of()
        );

        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("joao@email.com");
        assertThat(response.getRole()).isEqualTo(Role.PATIENT);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    private RegisterRequest buildRegisterRequest(String name, String email, String cpf, String password, Role role) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setCpf(cpf);
        request.setPassword(password);
        request.setRole(role);
        return request;
    }
}
