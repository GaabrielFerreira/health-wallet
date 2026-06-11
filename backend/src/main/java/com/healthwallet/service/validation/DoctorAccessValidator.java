package com.healthwallet.service.validation;

import com.healthwallet.exception.UnauthorizedDoctorAccessException;
import com.healthwallet.model.SharedReport;
import com.healthwallet.repository.SharedReportRepository;
import com.healthwallet.service.access.AccessExpirationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Componente com responsabilidade única (SRP) de validar se um médico possui
 * permissão ativa para acessar dados de um paciente.
 *
 * Delega a regra de expiração para AccessExpirationPolicy (DIP), evitando
 * duplicação da lógica de "está expirado?".
 */
@Component
@RequiredArgsConstructor
public class DoctorAccessValidator {

    private final SharedReportRepository sharedReportRepository;
    private final AccessExpirationPolicy expirationPolicy;

    public void requireActiveAccess(UUID doctorId, UUID patientId) {
        SharedReport access = sharedReportRepository
                .findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId)
                .orElseThrow(() -> new UnauthorizedDoctorAccessException(doctorId, patientId));

        if (!expirationPolicy.isActive(access)) {
            throw new UnauthorizedDoctorAccessException(doctorId, patientId);
        }
    }

    public boolean hasActiveAccess(UUID doctorId, UUID patientId) {
        return sharedReportRepository
                .findByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId)
                .map(expirationPolicy::isActive)
                .orElse(false);
    }
}
