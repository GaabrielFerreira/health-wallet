package com.healthwallet.service;

import com.healthwallet.dto.AnamnesisResponse;
import com.healthwallet.dto.GenerateShareLinkRequest;
import com.healthwallet.dto.PatientDataResponse;
import com.healthwallet.dto.ShareLinkResponse;
import com.healthwallet.exception.AnamnesisNotFoundException;
import com.healthwallet.exception.InvalidShareTokenException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.model.SharedReport;
import com.healthwallet.model.User;
import com.healthwallet.repository.SharedReportRepository;
import com.healthwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gerencia links de compartilhamento público por token (SCRUM-49).
 *
 * SOLID:
 * - SRP: responsabilidade única de gerar e validar tokens de link público.
 * - DIP: depende de abstrações (services existentes) para agregação de dados.
 */
@Service
@RequiredArgsConstructor
public class ShareLinkService {

    private final SharedReportRepository sharedReportRepository;
    private final UserRepository userRepository;
    private final AnamnesisService anamnesisService;
    private final VaccineService vaccineService;
    private final AppointmentService appointmentService;

    @Transactional
    public ShareLinkResponse generateLink(GenerateShareLinkRequest request) {
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(request.getPatientId()));

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(request.getExpiresInHours());

        SharedReport report = new SharedReport();
        report.setPatient(patient);
        report.setDoctor(null);
        report.setExpiresAt(expiresAt);
        report.setRevoked(false);

        SharedReport saved = sharedReportRepository.save(report);
        return new ShareLinkResponse(saved.getToken(), saved.getExpiresAt());
    }

    public PatientDataResponse getDataByToken(UUID token) {
        SharedReport report = sharedReportRepository.findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new InvalidShareTokenException(token));

        if (report.getExpiresAt() != null && report.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidShareTokenException(token);
        }

        UUID patientId = report.getPatient().getId();

        AnamnesisResponse anamnesis = null;
        try {
            anamnesis = anamnesisService.getByPatientId(patientId);
        } catch (AnamnesisNotFoundException ignored) {
        }

        return new PatientDataResponse(
                anamnesis,
                vaccineService.listByPatientId(patientId),
                appointmentService.listByPatientId(patientId, null, null, null, null)
        );
    }
}
