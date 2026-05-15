package com.healthwallet.service;

import com.healthwallet.dto.VaccineAttachmentResponse;
import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.model.AttachmentType;
import com.healthwallet.model.Dose;
import com.healthwallet.model.User;
import com.healthwallet.model.Vaccine;
import com.healthwallet.model.VaccineAttachment;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.repository.VaccineAttachmentRepository;
import com.healthwallet.repository.VaccineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaccineServiceTest {

    @Mock
    private VaccineRepository vaccineRepository;

    @Mock
    private VaccineAttachmentRepository vaccineAttachmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VaccineAttachmentService attachmentService;

    @InjectMocks
    private VaccineService vaccineService;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Test
    void create_savesVaccine_withPatientReference() {
        UUID patientId = UUID.randomUUID();
        User patient = buildUser(patientId);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(vaccineRepository.save(any())).thenAnswer(inv -> {
            Vaccine v = inv.getArgument(0);
            v.setId(UUID.randomUUID());
            return v;
        });

        VaccineResponse response = vaccineService.create(buildRequest(patientId));

        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getName()).isEqualTo("COVID-19");
        assertThat(response.isHasProof()).isFalse();
    }

    @Test
    void create_throwsPatientNotFound_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.create(buildRequest(patientId)))
                .isInstanceOf(PatientNotFoundException.class);

        verify(vaccineRepository, never()).save(any());
    }

    // ── LIST BY PATIENT ───────────────────────────────────────────────────────

    @Test
    void listByPatientId_flagsVaccinesThatHaveAttachment() {
        UUID patientId = UUID.randomUUID();
        User patient = buildUser(patientId);
        Vaccine withProof = buildVaccine(patient);
        Vaccine withoutProof = buildVaccine(patient);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(vaccineRepository.findByPatientId(patientId)).thenReturn(List.of(withProof, withoutProof));
        when(vaccineAttachmentRepository.findVaccineIdsWithAttachment(anyList()))
                .thenReturn(List.of(withProof.getId()));

        List<VaccineResponse> result = vaccineService.listByPatientId(patientId);

        assertThat(result).hasSize(2);
        assertThat(result.stream().filter(VaccineResponse::isHasProof).count()).isEqualTo(1);
    }

    @Test
    void listByPatientId_throwsPatientNotFound_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.listByPatientId(patientId))
                .isInstanceOf(PatientNotFoundException.class);
    }

    // ── DELEGAÇÃO ─────────────────────────────────────────────────────────────
    // Após refatoração SRP, métodos de attachment apenas delegam ao
    // VaccineAttachmentService. Os testes da lógica em si vivem em
    // VaccineAttachmentServiceTest.

    @Test
    void uploadProof_delegatesToAttachmentService() {
        UUID vaccineId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile("file", "c.pdf", "application/pdf", "x".getBytes());
        VaccineAttachmentResponse expected = new VaccineAttachmentResponse(
                UUID.randomUUID(), vaccineId, "c.pdf", "application/pdf", 1L, AttachmentType.PDF);

        when(attachmentService.upload(vaccineId, file)).thenReturn(expected);

        VaccineAttachmentResponse result = vaccineService.uploadProof(vaccineId, file);

        assertThat(result).isSameAs(expected);
        verify(attachmentService).upload(vaccineId, file);
    }

    @Test
    void getProof_delegatesToAttachmentService() {
        UUID vaccineId = UUID.randomUUID();
        VaccineAttachment expected = new VaccineAttachment();

        when(attachmentService.get(vaccineId)).thenReturn(expected);

        VaccineAttachment result = vaccineService.getProof(vaccineId);

        assertThat(result).isSameAs(expected);
        verify(attachmentService).get(vaccineId);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private User buildUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName("João Silva");
        user.setEmail("joao@email.com");
        return user;
    }

    private Vaccine buildVaccine(User patient) {
        Vaccine v = new Vaccine();
        v.setId(UUID.randomUUID());
        v.setPatient(patient);
        v.setName("COVID-19");
        v.setApplicationDate(LocalDate.of(2026, 1, 10));
        v.setDose(Dose.FIRST);
        return v;
    }

    private VaccineRequest buildRequest(UUID patientId) {
        VaccineRequest r = new VaccineRequest();
        r.setPatientId(patientId);
        r.setName("COVID-19");
        r.setApplicationDate(LocalDate.of(2026, 1, 10));
        r.setDose(Dose.FIRST);
        return r;
    }
}
