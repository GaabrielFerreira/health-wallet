package com.healthwallet.service.validation;

import com.healthwallet.exception.UnauthorizedDoctorAccessException;
import com.healthwallet.model.SharedReport;
import com.healthwallet.repository.SharedReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente com responsabilidade única (SRP) de validar se um médico possui
 * permissão ativa (não revogada e não expirada) para acessar dados de um paciente.
 *
 * Reutilizável em qualquer endpoint que exija checagem de permissão.
 */
@Component
@RequiredArgsConstructor
public class DoctorAccessValidator {

    private final SharedReportRepository sharedReportRepository;

    public void requireActiveAccess(UUID doctorId, UUID patientId) {
        SharedReport access = sharedReportRepository
                .findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId)
                .orElseThrow(() -> new UnauthorizedDoctorAccessException(doctorId, patientId));

        if (access.getExpiresAt() != null && access.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedDoctorAccessException(doctorId, patientId);
        }
    }

    public boolean hasActiveAccess(UUID doctorId, UUID patientId) {
        return sharedReportRepository
                .findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId)
                .filter(a -> a.getExpiresAt() == null || a.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }
}
