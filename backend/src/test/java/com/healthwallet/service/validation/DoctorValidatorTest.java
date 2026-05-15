package com.healthwallet.service.validation;

import com.healthwallet.exception.DoctorNotFoundException;
import com.healthwallet.exception.InvalidDoctorRoleException;
import com.healthwallet.model.Role;
import com.healthwallet.model.User;
import com.healthwallet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DoctorValidator validator;

    @Test
    void validateAndGet_returnsUser_whenRoleIsDoctor() {
        UUID doctorId = UUID.randomUUID();
        User doctor = buildUser(doctorId, Role.DOCTOR);

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        User result = validator.validateAndGet(doctorId);

        assertThat(result.getId()).isEqualTo(doctorId);
        assertThat(result.getRole()).isEqualTo(Role.DOCTOR);
    }

    @Test
    void validateAndGet_throwsDoctorNotFound_whenUserMissing() {
        UUID doctorId = UUID.randomUUID();
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateAndGet(doctorId))
                .isInstanceOf(DoctorNotFoundException.class)
                .hasMessageContaining(doctorId.toString());
    }

    @Test
    void validateAndGet_throwsInvalidDoctorRole_whenUserIsPatient() {
        UUID userId = UUID.randomUUID();
        User patient = buildUser(userId, Role.PATIENT);

        when(userRepository.findById(userId)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> validator.validateAndGet(userId))
                .isInstanceOf(InvalidDoctorRoleException.class)
                .hasMessageContaining("DOCTOR");
    }

    @Test
    void validateAndGet_throwsInvalidDoctorRole_whenUserIsAdmin() {
        UUID userId = UUID.randomUUID();
        User admin = buildUser(userId, Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> validator.validateAndGet(userId))
                .isInstanceOf(InvalidDoctorRoleException.class);
    }

    private User buildUser(UUID id, Role role) {
        User u = new User();
        u.setId(id);
        u.setName("Teste");
        u.setEmail("teste@email.com");
        u.setRole(role);
        return u;
    }
}
