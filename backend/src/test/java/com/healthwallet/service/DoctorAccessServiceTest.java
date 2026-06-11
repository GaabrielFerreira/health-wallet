package com.healthwallet.service;

import com.healthwallet.dto.DoctorAccessResponse;
import com.healthwallet.dto.GrantDoctorAccessRequest;
import com.healthwallet.exception.DoctorAccessAlreadyGrantedException;
import com.healthwallet.exception.DoctorNotFoundException;
import com.healthwallet.exception.InvalidDoctorRoleException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.exception.SharedAccessNotFoundException;
import com.healthwallet.model.Role;
import com.healthwallet.model.SharedReport;
import com.healthwallet.model.User;
import com.healthwallet.repository.SharedReportRepository;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.service.validation.DoctorValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorAccessServiceTest {

    @Mock
    private SharedReportRepository sharedReportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorValidator doctorValidator;

    @InjectMocks
    private DoctorAccessService service;

    // ── GRANT ─────────────────────────────────────────────────────────────────

    @Test
    void grantAccess_savesSharedReport_whenPatientAndDoctorAreValid() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        User patient = buildUser(patientId, Role.PATIENT, "João");
        User doctor = buildUser(doctorId, Role.DOCTOR, "Dra. Ana");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorValidator.validateAndGet(doctorId)).thenReturn(doctor);
        when(sharedReportRepository.existsByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId))
                .thenReturn(false);
        when(sharedReportRepository.save(any())).thenAnswer(inv -> {
            SharedReport a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        DoctorAccessResponse response = service.grantAccess(buildRequest(patientId, doctorId));

        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getDoctorId()).isEqualTo(doctorId);
        assertThat(response.getDoctorName()).isEqualTo("Dra. Ana");
        assertThat(response.getRevoked()).isFalse();

        ArgumentCaptor<SharedReport> captor = ArgumentCaptor.forClass(SharedReport.class);
        verify(sharedReportRepository).save(captor.capture());
        assertThat(captor.getValue().getPatient().getId()).isEqualTo(patientId);
        assertThat(captor.getValue().getDoctor().getId()).isEqualTo(doctorId);
        assertThat(captor.getValue().getRevoked()).isFalse();
    }

    @Test
    void grantAccess_throwsPatientNotFound_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.grantAccess(buildRequest(patientId, UUID.randomUUID())))
                .isInstanceOf(PatientNotFoundException.class);

        verify(sharedReportRepository, never()).save(any());
        verify(doctorValidator, never()).validateAndGet(any());
    }

    @Test
    void grantAccess_throwsDoctorNotFound_whenDoctorMissing() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        when(userRepository.findById(patientId)).thenReturn(Optional.of(buildUser(patientId, Role.PATIENT, "Patient")));
        when(doctorValidator.validateAndGet(doctorId)).thenThrow(new DoctorNotFoundException(doctorId));

        assertThatThrownBy(() -> service.grantAccess(buildRequest(patientId, doctorId)))
                .isInstanceOf(DoctorNotFoundException.class);

        verify(sharedReportRepository, never()).save(any());
    }

    @Test
    void grantAccess_throwsInvalidDoctorRole_whenUserIsNotDoctor() {
        UUID patientId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(patientId)).thenReturn(Optional.of(buildUser(patientId, Role.PATIENT, "Patient")));
        when(doctorValidator.validateAndGet(userId)).thenThrow(new InvalidDoctorRoleException(userId));

        assertThatThrownBy(() -> service.grantAccess(buildRequest(patientId, userId)))
                .isInstanceOf(InvalidDoctorRoleException.class);

        verify(sharedReportRepository, never()).save(any());
    }

    @Test
    void grantAccess_throwsAlreadyGranted_whenActiveAccessExists() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        User patient = buildUser(patientId, Role.PATIENT, "João");
        User doctor = buildUser(doctorId, Role.DOCTOR, "Dra. Ana");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorValidator.validateAndGet(doctorId)).thenReturn(doctor);
        when(sharedReportRepository.existsByPatientIdAndDoctorIdAndRevokedFalse(patientId, doctorId))
                .thenReturn(true);

        assertThatThrownBy(() -> service.grantAccess(buildRequest(patientId, doctorId)))
                .isInstanceOf(DoctorAccessAlreadyGrantedException.class);

        verify(sharedReportRepository, never()).save(any());
    }

    // ── REVOKE ────────────────────────────────────────────────────────────────

    @Test
    void revokeAccess_setsRevokedTrue_whenAccessExists() {
        UUID accessId = UUID.randomUUID();
        SharedReport access = new SharedReport();
        access.setId(accessId);
        access.setRevoked(false);

        when(sharedReportRepository.findById(accessId)).thenReturn(Optional.of(access));

        service.revokeAccess(accessId);

        ArgumentCaptor<SharedReport> captor = ArgumentCaptor.forClass(SharedReport.class);
        verify(sharedReportRepository).save(captor.capture());
        assertThat(captor.getValue().getRevoked()).isTrue();
    }

    @Test
    void revokeAccess_throwsSharedAccessNotFound_whenAccessMissing() {
        UUID accessId = UUID.randomUUID();
        when(sharedReportRepository.findById(accessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revokeAccess(accessId))
                .isInstanceOf(SharedAccessNotFoundException.class);

        verify(sharedReportRepository, never()).save(any());
    }

    // ── LIST ──────────────────────────────────────────────────────────────────

    @Test
    void listByPatient_returnsAllAccesses() {
        UUID patientId = UUID.randomUUID();
        User patient = buildUser(patientId, Role.PATIENT, "João");
        User doctor = buildUser(UUID.randomUUID(), Role.DOCTOR, "Dra. Ana");

        SharedReport access = new SharedReport();
        access.setId(UUID.randomUUID());
        access.setPatient(patient);
        access.setDoctor(doctor);
        access.setRevoked(false);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(sharedReportRepository.findByPatientIdAndDoctorIsNotNullOrderByCreatedAtDesc(patientId))
                .thenReturn(List.of(access));

        List<DoctorAccessResponse> result = service.listByPatient(patientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDoctorName()).isEqualTo("Dra. Ana");
    }

    @Test
    void listByPatient_throwsPatientNotFound_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listByPatient(patientId))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void listActiveByDoctor_returnsList() {
        UUID doctorId = UUID.randomUUID();
        User doctor = buildUser(doctorId, Role.DOCTOR, "Dra. Ana");
        User patient = buildUser(UUID.randomUUID(), Role.PATIENT, "João");

        SharedReport access = new SharedReport();
        access.setId(UUID.randomUUID());
        access.setPatient(patient);
        access.setDoctor(doctor);
        access.setRevoked(false);

        when(doctorValidator.validateAndGet(doctorId)).thenReturn(doctor);
        when(sharedReportRepository.findByDoctorIdAndRevokedFalseOrderByCreatedAtDesc(doctorId))
                .thenReturn(List.of(access));

        List<DoctorAccessResponse> result = service.listActiveByDoctor(doctorId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(patient.getId());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private User buildUser(UUID id, Role role, String name) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(name.toLowerCase().replace(" ", "") + "@email.com");
        u.setRole(role);
        return u;
    }

    private GrantDoctorAccessRequest buildRequest(UUID patientId, UUID doctorId) {
        GrantDoctorAccessRequest r = new GrantDoctorAccessRequest();
        r.setPatientId(patientId);
        r.setDoctorId(doctorId);
        r.setDataTypes("ANAMNESIS,VACCINES");
        r.setExpiresAt(LocalDateTime.now().plusDays(30));
        return r;
    }
}
