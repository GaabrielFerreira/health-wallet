package com.healthwallet.service;

import com.healthwallet.dto.VaccineAttachmentResponse;
import com.healthwallet.exception.InvalidAttachmentException;
import com.healthwallet.exception.VaccineAttachmentNotFoundException;
import com.healthwallet.exception.VaccineNotFoundException;
import com.healthwallet.model.AttachmentType;
import com.healthwallet.model.Dose;
import com.healthwallet.model.User;
import com.healthwallet.model.Vaccine;
import com.healthwallet.model.VaccineAttachment;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaccineAttachmentServiceTest {

    @Mock
    private VaccineRepository vaccineRepository;

    @Mock
    private VaccineAttachmentRepository vaccineAttachmentRepository;

    @InjectMocks
    private VaccineAttachmentService attachmentService;

    @Test
    void upload_savesAttachment_whenFileIsValid() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(vaccineId);
        MultipartFile file = new MockMultipartFile(
                "file", "comprovante.pdf", "application/pdf", "conteudo-pdf".getBytes());

        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));
        when(vaccineAttachmentRepository.findByVaccineId(vaccineId)).thenReturn(Optional.empty());
        when(vaccineAttachmentRepository.save(any())).thenAnswer(inv -> {
            VaccineAttachment a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        VaccineAttachmentResponse response = attachmentService.upload(vaccineId, file);

        assertThat(response.getVaccineId()).isEqualTo(vaccineId);
        assertThat(response.getFileName()).isEqualTo("comprovante.pdf");
        assertThat(response.getType()).isEqualTo(AttachmentType.PDF);

        ArgumentCaptor<VaccineAttachment> captor = ArgumentCaptor.forClass(VaccineAttachment.class);
        verify(vaccineAttachmentRepository).save(captor.capture());
        assertThat(captor.getValue().getVaccine().getId()).isEqualTo(vaccineId);
        assertThat(captor.getValue().getData()).isEqualTo("conteudo-pdf".getBytes());
    }

    @Test
    void upload_replacesExistingAttachment() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(vaccineId);
        VaccineAttachment existing = new VaccineAttachment();
        existing.setId(UUID.randomUUID());
        MultipartFile file = new MockMultipartFile(
                "file", "novo.png", "image/png", "png-bytes".getBytes());

        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));
        when(vaccineAttachmentRepository.findByVaccineId(vaccineId)).thenReturn(Optional.of(existing));
        when(vaccineAttachmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VaccineAttachmentResponse response = attachmentService.upload(vaccineId, file);

        assertThat(response.getId()).isEqualTo(existing.getId());
        assertThat(response.getType()).isEqualTo(AttachmentType.PNG);
    }

    @Test
    void upload_throwsVaccineNotFound_whenVaccineMissing() {
        UUID vaccineId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile(
                "file", "c.pdf", "application/pdf", "x".getBytes());
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.upload(vaccineId, file))
                .isInstanceOf(VaccineNotFoundException.class);

        verify(vaccineAttachmentRepository, never()).save(any());
    }

    @Test
    void upload_throwsInvalidAttachment_whenFileIsEmpty() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(vaccineId);
        MultipartFile file = new MockMultipartFile(
                "file", "c.pdf", "application/pdf", new byte[0]);
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));

        assertThatThrownBy(() -> attachmentService.upload(vaccineId, file))
                .isInstanceOf(InvalidAttachmentException.class);
    }

    @Test
    void upload_throwsInvalidAttachment_whenContentTypeNotAllowed() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(vaccineId);
        MultipartFile file = new MockMultipartFile(
                "file", "c.txt", "text/plain", "texto".getBytes());
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));

        assertThatThrownBy(() -> attachmentService.upload(vaccineId, file))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("Tipo de arquivo");

        verify(vaccineAttachmentRepository, never()).save(any());
    }

    @Test
    void get_returnsAttachment_whenPresent() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(vaccineId);
        VaccineAttachment attachment = new VaccineAttachment();
        attachment.setData("bytes".getBytes());

        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));
        when(vaccineAttachmentRepository.findByVaccineId(vaccineId)).thenReturn(Optional.of(attachment));

        VaccineAttachment result = attachmentService.get(vaccineId);

        assertThat(result.getData()).isEqualTo("bytes".getBytes());
    }

    @Test
    void get_throwsVaccineNotFound_whenVaccineMissing() {
        UUID vaccineId = UUID.randomUUID();
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.get(vaccineId))
                .isInstanceOf(VaccineNotFoundException.class);
    }

    @Test
    void get_throwsAttachmentNotFound_whenNoAttachment() {
        UUID vaccineId = UUID.randomUUID();
        Vaccine vaccine = buildVaccine(vaccineId);
        when(vaccineRepository.findById(vaccineId)).thenReturn(Optional.of(vaccine));
        when(vaccineAttachmentRepository.findByVaccineId(vaccineId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.get(vaccineId))
                .isInstanceOf(VaccineAttachmentNotFoundException.class);
    }

    private Vaccine buildVaccine(UUID id) {
        User patient = new User();
        patient.setId(UUID.randomUUID());
        patient.setName("João");
        Vaccine v = new Vaccine();
        v.setId(id);
        v.setPatient(patient);
        v.setName("COVID-19");
        v.setApplicationDate(LocalDate.of(2026, 1, 10));
        v.setDose(Dose.FIRST);
        return v;
    }
}
