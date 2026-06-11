package com.healthwallet.service;

import com.healthwallet.dto.DoctorAccessResponse;
import com.healthwallet.dto.GrantDoctorAccessRequest;
import com.healthwallet.dto.RenewAccessRequest;
import com.healthwallet.exception.CannotRenewRevokedAccessException;
import com.healthwallet.exception.DoctorAccessAlreadyGrantedException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.exception.SharedAccessNotFoundException;
import com.healthwallet.model.SharedReport;
import com.healthwallet.model.User;
import com.healthwallet.repository.SharedReportRepository;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.service.access.AccessExpirationPolicy;
import com.healthwallet.service.validation.DoctorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsável por gerenciar permissões de acesso de médicos a dados de pacientes.
 * SCRUM-50: conceder, revogar e listar. SCRUM-52: renovar expiração e consultar status.
 *
 * Aplica SOLID:
 * - SRP: cuida do ciclo de vida da permissão. Validação de médico em DoctorValidator,
 *   regra de expiração em AccessExpirationPolicy.
 * - DIP: depende das abstrações DoctorValidator, AccessExpirationPolicy e repositórios.
 */
@Service
@RequiredArgsConstructor
public class DoctorAccessService {

    private final SharedReportRepository sharedReportRepository;
    private final UserRepository userRepository;
    private final DoctorValidator doctorValidator;
    private final AccessExpirationPolicy expirationPolicy;

    @Transactional
    public DoctorAccessResponse grantAccess(GrantDoctorAccessRequest request) {
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(request.getPatientId()));

        User doctor = doctorValidator.validateAndGet(request.getDoctorId());

        if (sharedReportRepository.existsByPatientIdAndDoctorIdAndRevokedFalse(
                patient.getId(), doctor.getId())) {
            throw new DoctorAccessAlreadyGrantedException(doctor.getId());
        }

        SharedReport access = new SharedReport();
        access.setPatient(patient);
        access.setDoctor(doctor);
        access.setDataTypes(request.getDataTypes());
        access.setExpiresAt(request.getExpiresAt());
        access.setRevoked(false);

        return toResponse(sharedReportRepository.save(access));
    }

    @Transactional
    public void revokeAccess(UUID accessId) {
        SharedReport access = sharedReportRepository.findById(accessId)
                .orElseThrow(() -> new SharedAccessNotFoundException(accessId));

        access.setRevoked(true);
        sharedReportRepository.save(access);
    }

    @Transactional
    public DoctorAccessResponse renewAccess(UUID accessId, RenewAccessRequest request) {
        SharedReport access = sharedReportRepository.findById(accessId)
                .orElseThrow(() -> new SharedAccessNotFoundException(accessId));

        if (Boolean.TRUE.equals(access.getRevoked())) {
            throw new CannotRenewRevokedAccessException(accessId);
        }

        access.setExpiresAt(request.getExpiresAt());
        return toResponse(sharedReportRepository.save(access));
    }

    public DoctorAccessResponse getById(UUID accessId) {
        SharedReport access = sharedReportRepository.findById(accessId)
                .orElseThrow(() -> new SharedAccessNotFoundException(accessId));
        return toResponse(access);
    }

    public List<DoctorAccessResponse> listByPatient(UUID patientId) {
        userRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        return sharedReportRepository
                .findByPatientIdAndDoctorIsNotNullOrderByCreatedAtDesc(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<DoctorAccessResponse> listActiveByDoctor(UUID doctorId) {
        doctorValidator.validateAndGet(doctorId);

        return sharedReportRepository
                .findByDoctorIdAndRevokedFalseOrderByCreatedAtDesc(doctorId)
                .stream()
                .filter(expirationPolicy::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DoctorAccessResponse toResponse(SharedReport access) {
        User doctor = access.getDoctor();
        return new DoctorAccessResponse(
                access.getId(),
                access.getPatient().getId(),
                doctor == null ? null : doctor.getId(),
                doctor == null ? null : doctor.getName(),
                doctor == null ? null : doctor.getEmail(),
                access.getDataTypes(),
                access.getCreatedAt(),
                access.getExpiresAt(),
                access.getRevoked(),
                expirationPolicy.statusOf(access)
        );
    }
}
