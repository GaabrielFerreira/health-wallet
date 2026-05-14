package com.healthwallet.service;

import com.healthwallet.dto.VaccineAttachmentResponse;
import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.exception.InvalidAttachmentException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.exception.VaccineAttachmentNotFoundException;
import com.healthwallet.exception.VaccineNotFoundException;
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
import org.mockito.ArgumentCaptor;
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

    // ── UPLOAD PROOF ──────────────────────────────────────────────────────────

    @Test
    void uploadProof_savesAttachment_whenFileIsValid() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(buildUser(UUID.randomUUID()));
        vaccine.setId(vaccineId);
        MultipartFile file = new MockMultipartFile(
                "file", "comprovante.pdf", "application/pdf", "conteudo-pdf".getBytes());

        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));
        when(vaccineAttachmentRepository.findByVaccineId(vaccineId)).thenReturn(Optional.empty());
        when(vaccineAttachmentRepository.save(any())).thenAnswer(inv -> {
            VaccineAttachment a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        VaccineAttachmentResponse response = vaccineService.uploadProof(vaccineId, file);

        assertThat(response.getVaccineId()).isEqualTo(vaccineId);
        assertThat(response.getFileName()).isEqualTo("comprovante.pdf");
        assertThat(response.getType()).isEqualTo(AttachmentType.PDF);

        ArgumentCaptor<VaccineAttachment> captor = ArgumentCaptor.forClass(VaccineAttachment.class);
        verify(vaccineAttachmentRepository).save(captor.capture());
        assertThat(captor.getValue().getVaccine().getId()).isEqualTo(vaccineId);
        assertThat(captor.getValue().getData()).isEqualTo("conteudo-pdf".getBytes());
    }

    @Test
    void uploadProof_replacesExistingAttachment() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(buildUser(UUID.randomUUID()));
        vaccine.setId(vaccineId);
        VaccineAttachment existing = new VaccineAttachment();
        existing.setId(UUID.randomUUID());
        MultipartFile file = new MockMultipartFile(
                "file", "novo.png", "image/png", "png-bytes".getBytes());

        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));
        when(vaccineAttachmentRepository.findByVaccineId(vaccineId)).thenReturn(Optional.of(existing));
        when(vaccineAttachmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VaccineAttachmentResponse response = vaccineService.uploadProof(vaccineId, file);

        assertThat(response.getId()).isEqualTo(existing.getId());
        assertThat(response.getType()).isEqualTo(AttachmentType.PNG);
    }

    @Test
    void uploadProof_throwsVaccineNotFound_whenVaccineMissing() {
        UUID vaccineId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile(
                "file", "c.pdf", "application/pdf", "x".getBytes());
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.uploadProof(vaccineId, file))
                .isInstanceOf(VaccineNotFoundException.class);

        verify(vaccineAttachmentRepository, never()).save(any());
    }

    @Test
    void uploadProof_throwsInvalidAttachment_whenFileIsEmpty() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(buildUser(UUID.randomUUID()));
        vaccine.setId(vaccineId);
        MultipartFile file = new MockMultipartFile(
                "file", "c.pdf", "application/pdf", new byte[0]);
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));

        assertThatThrownBy(() -> vaccineService.uploadProof(vaccineId, file))
                .isInstanceOf(InvalidAttachmentException.class);
    }

    @Test
    void uploadProof_throwsInvalidAttachment_whenContentTypeNotAllowed() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(buildUser(UUID.randomUUID()));
        vaccine.setId(vaccineId);
        MultipartFile file = new MockMultipartFile(
                "file", "c.txt", "text/plain", "texto".getBytes());
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));

        assertThatThrownBy(() -> vaccineService.uploadProof(vaccineId, file))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("Tipo de arquivo");

        verify(vaccineAttachmentRepository, never()).save(any());
    }

    // ── GET PROOF ─────────────────────────────────────────────────────────────

    @Test
    void getProof_returnsAttachment_whenPresent() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(buildUser(UUID.randomUUID()));
        vaccine.setId(vaccineId);
        VaccineAttachment attachment = new VaccineAttachment();
        attachment.setData("bytes".getBytes());

        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));
        when(vaccineAttachmentRepository.findByVaccineId(vaccineId)).thenReturn(Optional.of(attachment));

        VaccineAttachment result = vaccineService.getProof(vaccineId);

        assertThat(result.getData()).isEqualTo("bytes".getBytes());
    }

    @Test
    void getProof_throwsVaccineNotFound_whenVaccineMissing() {
        UUID vaccineId = UUID.randomUUID();
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.getProof(vaccineId))
                .isInstanceOf(VaccineNotFoundException.class);
    }

    @Test
    void getProof_throwsAttachmentNotFound_whenNoAttachment() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(buildUser(UUID.randomUUID()));
        vaccine.setId(vaccineId);
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));
        when(vaccineAttachmentRepository.findByVaccineId(vaccineId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.getProof(vaccineId))
                .isInstanceOf(VaccineAttachmentNotFoundException.class);
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
