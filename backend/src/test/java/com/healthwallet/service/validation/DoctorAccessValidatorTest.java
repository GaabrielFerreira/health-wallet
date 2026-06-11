package com.healthwallet.service.validation;

import com.healthwallet.exception.UnauthorizedDoctorAccessException;
import com.healthwallet.model.SharedReport;
import com.healthwallet.repository.SharedReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorAccessValidatorTest {

    @Mock
    private SharedReportRepository sharedReportRepository;

    @InjectMocks
    private DoctorAccessValidator validator;

    @Test
    void requireActiveAccess_doesNotThrow_whenAccessIsActiveAndNotExpired() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        SharedReport access = new SharedReport();
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(sharedReportRepository.findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId))
                .thenReturn(Optional.of(access));

        assertThatCode(() -> validator.requireActiveAccess(doctorId, patientId))
                .doesNotThrowAnyException();
    }

    @Test
    void requireActiveAccess_doesNotThrow_whenExpiresAtIsNull() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        SharedReport access = new SharedReport();
        access.setRevoked(false);
        access.setExpiresAt(null);

        when(sharedReportRepository.findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId))
                .thenReturn(Optional.of(access));

        assertThatCode(() -> validator.requireActiveAccess(doctorId, patientId))
                .doesNotThrowAnyException();
    }

    @Test
    void requireActiveAccess_throws_whenNoAccessExists() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        when(sharedReportRepository.findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.requireActiveAccess(doctorId, patientId))
                .isInstanceOf(UnauthorizedDoctorAccessException.class);
    }

    @Test
    void requireActiveAccess_throws_whenAccessIsExpired() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        SharedReport access = new SharedReport();
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(sharedReportRepository.findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId))
                .thenReturn(Optional.of(access));

        assertThatThrownBy(() -> validator.requireActiveAccess(doctorId, patientId))
                .isInstanceOf(UnauthorizedDoctorAccessException.class);
    }

    @Test
    void hasActiveAccess_returnsTrue_whenAccessIsActive() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        SharedReport access = new SharedReport();
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(sharedReportRepository.findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId))
                .thenReturn(Optional.of(access));

        assertThat(validator.hasActiveAccess(doctorId, patientId)).isTrue();
    }

    @Test
    void hasActiveAccess_returnsFalse_whenNoAccessExists() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        when(sharedReportRepository.findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId))
                .thenReturn(Optional.empty());

        assertThat(validator.hasActiveAccess(doctorId, patientId)).isFalse();
    }
}
