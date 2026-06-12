package com.healthwallet.service;

import com.healthwallet.dto.DoctorAccessResponse;
import com.healthwallet.dto.DoctorLookupResponse;
import com.healthwallet.dto.GrantDoctorAccessRequest;
import com.healthwallet.dto.RenewAccessRequest;
import com.healthwallet.exception.CannotRenewRevokedAccessException;
import com.healthwallet.exception.DoctorAccessAlreadyGrantedException;
import com.healthwallet.exception.DoctorNotFoundException;
import com.healthwallet.exception.InvalidDoctorRoleException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.exception.SharedAccessNotFoundException;
import com.healthwallet.model.AccessStatus;
import com.healthwallet.model.Role;
import com.healthwallet.model.SharedReport;
import com.healthwallet.model.User;
import com.healthwallet.repository.SharedReportRepository;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.service.access.AccessExpirationPolicy;
import com.healthwallet.service.validation.DoctorValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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

    // policy é lógica pura — usamos a instância real via @Spy
    @Spy
    private AccessExpirationPolicy expirationPolicy = new AccessExpirationPolicy();

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
        access.setExpiresAt(LocalDateTime.now().plusDays(10));

        when(doctorValidator.validateAndGet(doctorId)).thenReturn(doctor);
        when(sharedReportRepository.findByDoctorIdAndRevokedFalseOrderByCreatedAtDesc(doctorId))
                .thenReturn(List.of(access));

        List<DoctorAccessResponse> result = service.listActiveByDoctor(doctorId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(patient.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(AccessStatus.ACTIVE);
    }

    @Test
    void listActiveByDoctor_excludesExpiredAccesses() {
        UUID doctorId = UUID.randomUUID();
        User doctor = buildUser(doctorId, Role.DOCTOR, "Dra. Ana");
        User patient = buildUser(UUID.randomUUID(), Role.PATIENT, "João");

        SharedReport activeAccess = new SharedReport();
        activeAccess.setId(UUID.randomUUID());
        activeAccess.setPatient(patient);
        activeAccess.setDoctor(doctor);
        activeAccess.setRevoked(false);
        activeAccess.setExpiresAt(LocalDateTime.now().plusDays(5));

        SharedReport expiredAccess = new SharedReport();
        expiredAccess.setId(UUID.randomUUID());
        expiredAccess.setPatient(patient);
        expiredAccess.setDoctor(doctor);
        expiredAccess.setRevoked(false);
        expiredAccess.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(doctorValidator.validateAndGet(doctorId)).thenReturn(doctor);
        when(sharedReportRepository.findByDoctorIdAndRevokedFalseOrderByCreatedAtDesc(doctorId))
                .thenReturn(List.of(activeAccess, expiredAccess));

        List<DoctorAccessResponse> result = service.listActiveByDoctor(doctorId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(activeAccess.getId());
    }

    // ── RENEW ─────────────────────────────────────────────────────────────────

    @Test
    void renewAccess_updatesExpiration_whenAccessIsNotRevoked() {
        UUID accessId = UUID.randomUUID();
        User patient = buildUser(UUID.randomUUID(), Role.PATIENT, "João");
        User doctor = buildUser(UUID.randomUUID(), Role.DOCTOR, "Dra. Ana");

        SharedReport access = new SharedReport();
        access.setId(accessId);
        access.setPatient(patient);
        access.setDoctor(doctor);
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().plusDays(1));

        LocalDateTime newExpiry = LocalDateTime.now().plusDays(60);
        RenewAccessRequest request = new RenewAccessRequest();
        request.setExpiresAt(newExpiry);

        when(sharedReportRepository.findById(accessId)).thenReturn(Optional.of(access));
        when(sharedReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DoctorAccessResponse response = service.renewAccess(accessId, request);

        assertThat(response.getExpiresAt()).isEqualTo(newExpiry);
        assertThat(response.getStatus()).isEqualTo(AccessStatus.ACTIVE);
    }

    @Test
    void renewAccess_throwsCannotRenewRevoked_whenAccessIsRevoked() {
        UUID accessId = UUID.randomUUID();
        SharedReport access = new SharedReport();
        access.setId(accessId);
        access.setRevoked(true);

        RenewAccessRequest request = new RenewAccessRequest();
        request.setExpiresAt(LocalDateTime.now().plusDays(30));

        when(sharedReportRepository.findById(accessId)).thenReturn(Optional.of(access));

        assertThatThrownBy(() -> service.renewAccess(accessId, request))
                .isInstanceOf(CannotRenewRevokedAccessException.class);

        verify(sharedReportRepository, never()).save(any());
    }

    @Test
    void renewAccess_throwsSharedAccessNotFound_whenAccessMissing() {
        UUID accessId = UUID.randomUUID();
        RenewAccessRequest request = new RenewAccessRequest();
        request.setExpiresAt(LocalDateTime.now().plusDays(30));

        when(sharedReportRepository.findById(accessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.renewAccess(accessId, request))
                .isInstanceOf(SharedAccessNotFoundException.class);
    }

    // ── STATUS (getById) ──────────────────────────────────────────────────────

    @Test
    void getById_returnsRevokedStatus_whenAccessIsRevoked() {
        UUID accessId = UUID.randomUUID();
        SharedReport access = buildLinkedAccess(accessId);
        access.setRevoked(true);

        when(sharedReportRepository.findById(accessId)).thenReturn(Optional.of(access));

        assertThat(service.getById(accessId).getStatus()).isEqualTo(AccessStatus.REVOKED);
    }

    @Test
    void getById_returnsExpiredStatus_whenAccessIsExpired() {
        UUID accessId = UUID.randomUUID();
        SharedReport access = buildLinkedAccess(accessId);
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(sharedReportRepository.findById(accessId)).thenReturn(Optional.of(access));

        assertThat(service.getById(accessId).getStatus()).isEqualTo(AccessStatus.EXPIRED);
    }

    @Test
    void getById_returnsActiveStatus_whenAccessIsActive() {
        UUID accessId = UUID.randomUUID();
        SharedReport access = buildLinkedAccess(accessId);
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().plusDays(3));

        when(sharedReportRepository.findById(accessId)).thenReturn(Optional.of(access));

        assertThat(service.getById(accessId).getStatus()).isEqualTo(AccessStatus.ACTIVE);
    }

    @Test
    void getById_throwsSharedAccessNotFound_whenAccessMissing() {
        UUID accessId = UUID.randomUUID();
        when(sharedReportRepository.findById(accessId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(accessId))
                .isInstanceOf(SharedAccessNotFoundException.class);
    }

    // ── FIND DOCTOR BY EMAIL (SCRUM-55) ─────────────────────────────────────────

    @Test
    void findDoctorByEmail_returnsDoctor_whenEmailBelongsToDoctor() {
        User doctor = buildUser(UUID.randomUUID(), Role.DOCTOR, "Dra. Ana");
        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));

        DoctorLookupResponse response = service.findDoctorByEmail(doctor.getEmail());

        assertThat(response.getId()).isEqualTo(doctor.getId());
        assertThat(response.getName()).isEqualTo("Dra. Ana");
        assertThat(response.getEmail()).isEqualTo(doctor.getEmail());
    }

    @Test
    void findDoctorByEmail_throwsDoctorNotFound_whenEmailNotFound() {
        when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findDoctorByEmail("naoexiste@email.com"))
                .isInstanceOf(DoctorNotFoundException.class);
    }

    @Test
    void findDoctorByEmail_throwsInvalidDoctorRole_whenUserIsNotDoctor() {
        User patient = buildUser(UUID.randomUUID(), Role.PATIENT, "João");
        when(userRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> service.findDoctorByEmail(patient.getEmail()))
                .isInstanceOf(InvalidDoctorRoleException.class);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private SharedReport buildLinkedAccess(UUID accessId) {
        SharedReport access = new SharedReport();
        access.setId(accessId);
        access.setPatient(buildUser(UUID.randomUUID(), Role.PATIENT, "João"));
        access.setDoctor(buildUser(UUID.randomUUID(), Role.DOCTOR, "Dra. Ana"));
        return access;
    }

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
