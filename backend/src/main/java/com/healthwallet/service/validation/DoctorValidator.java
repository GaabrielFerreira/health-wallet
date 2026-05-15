package com.healthwallet.service.validation;

import com.healthwallet.exception.DoctorNotFoundException;
import com.healthwallet.exception.InvalidDoctorRoleException;
import com.healthwallet.model.Role;
import com.healthwallet.model.User;
import com.healthwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Componente com responsabilidade única (SRP) de validar e recuperar um médico
 * a partir de um UUID. Garante que o usuário existe e possui o papel DOCTOR
 * antes de ser usado em qualquer contexto (consultas, etc).
 */
@Component
@RequiredArgsConstructor
public class DoctorValidator {

    private final UserRepository userRepository;

    public User validateAndGet(UUID doctorId) {
        User user = userRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));

        if (user.getRole() != Role.DOCTOR) {
            throw new InvalidDoctorRoleException(doctorId);
        }
        return user;
    }
}
