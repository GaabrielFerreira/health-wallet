package com.healthwallet.service;

import com.healthwallet.dto.RegisterRequest;
import com.healthwallet.dto.RegisterResponse;
import com.healthwallet.exception.EmailAlreadyExistsException;
import com.healthwallet.model.User;
import com.healthwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setCpf(request.getCpf());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User saved = userRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getName(), saved.getEmail());
    }
}
