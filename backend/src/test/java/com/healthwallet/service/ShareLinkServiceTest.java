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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareLinkServiceTest {

    @Mock
    private SharedReportRepository sharedReportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnamnesisService anamnesisService;

    @Mock
    private VaccineService vaccineService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private ShareLinkService service;

    @Test
    void generateLink_createsTokenWithFutureExpiry() {
        UUID patientId = UUID.randomUUID();
        User patient = new User();
        patient.setId(patientId);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(sharedReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GenerateShareLinkRequest request = new GenerateShareLinkRequest();
        request.setPatientId(patientId);
        request.setExpiresInHours(24);

        ShareLinkResponse response = service.generateLink(request);

        assertThat(response.getToken()).isNotNull();
        assertThat(response.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void generateLink_throwsPatientNotFound_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        GenerateShareLinkRequest request = new GenerateShareLinkRequest();
        request.setPatientId(patientId);

        assertThatThrownBy(() -> service.generateLink(request))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void getDataByToken_returnsAggregatedData_whenTokenValid() {
        UUID token = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        SharedReport report = buildReport(patientId, LocalDateTime.now().plusHours(1));

        AnamnesisResponse anamnesis = buildAnamnesis(patientId);
        when(sharedReportRepository.findByTokenAndRevokedFalse(token)).thenReturn(Optional.of(report));
        when(anamnesisService.getByPatientId(patientId)).thenReturn(anamnesis);
        when(vaccineService.listByPatientId(patientId)).thenReturn(List.of());
        when(appointmentService.listByPatientId(patientId, null, null, null, null)).thenReturn(List.of());

        PatientDataResponse data = service.getDataByToken(token);

        assertThat(data.getAnamnesis()).isEqualTo(anamnesis);
        assertThat(data.getVaccines()).isEmpty();
        assertThat(data.getAppointments()).isEmpty();
    }

    @Test
    void getDataByToken_throwsInvalidToken_whenTokenNotFound() {
        UUID token = UUID.randomUUID();
        when(sharedReportRepository.findByTokenAndRevokedFalse(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDataByToken(token))
                .isInstanceOf(InvalidShareTokenException.class);
    }

    @Test
    void getDataByToken_throwsInvalidToken_whenExpired() {
        UUID token = UUID.randomUUID();
        SharedReport report = buildReport(UUID.randomUUID(), LocalDateTime.now().minusMinutes(1));
        when(sharedReportRepository.findByTokenAndRevokedFalse(token)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.getDataByToken(token))
                .isInstanceOf(InvalidShareTokenException.class);
    }

    @Test
    void getDataByToken_returnsNullAnamnesis_whenAnamnesisMissing() {
        UUID token = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        SharedReport report = buildReport(patientId, LocalDateTime.now().plusHours(1));

        when(sharedReportRepository.findByTokenAndRevokedFalse(token)).thenReturn(Optional.of(report));
        when(anamnesisService.getByPatientId(patientId)).thenThrow(new AnamnesisNotFoundException(patientId));
        when(vaccineService.listByPatientId(patientId)).thenReturn(List.of());
        when(appointmentService.listByPatientId(patientId, null, null, null, null)).thenReturn(List.of());

        PatientDataResponse data = service.getDataByToken(token);

        assertThat(data.getAnamnesis()).isNull();
    }

    private SharedReport buildReport(UUID patientId, LocalDateTime expiresAt) {
        User patient = new User();
        patient.setId(patientId);

        SharedReport report = new SharedReport();
        report.setPatient(patient);
        report.setExpiresAt(expiresAt);
        report.setRevoked(false);
        return report;
    }

    private AnamnesisResponse buildAnamnesis(UUID patientId) {
        return new AnamnesisResponse(
                UUID.randomUUID(), patientId, "alergia a dipirona", null, null, "O+", null, null,
                null, null, null, null, null, null);
    }
}
