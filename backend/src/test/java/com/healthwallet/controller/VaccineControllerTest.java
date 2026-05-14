package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.VaccineAttachmentResponse;
import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.exception.InvalidAttachmentException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.exception.VaccineAttachmentNotFoundException;
import com.healthwallet.exception.VaccineNotFoundException;
import com.healthwallet.model.AttachmentType;
import com.healthwallet.model.Dose;
import com.healthwallet.model.VaccineAttachment;
import com.healthwallet.security.JwtService;
import com.healthwallet.security.UserDetailsServiceImpl;
import com.healthwallet.service.VaccineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VaccineController.class)
@AutoConfigureMockMvc(addFilters = false)
class VaccineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VaccineService vaccineService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Test
    void create_returns201_withPatientIdReference() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID vaccineId = UUID.randomUUID();

        when(vaccineService.create(any())).thenReturn(buildResponse(vaccineId, patientId));

        mockMvc.perform(post("/api/vaccines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(vaccineId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.name").value("COVID-19"));
    }

    @Test
    void create_returns400_whenNameIsBlank() throws Exception {
        VaccineRequest request = buildRequest(UUID.randomUUID());
        request.setName("");

        mockMvc.perform(post("/api/vaccines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── LIST ──────────────────────────────────────────────────────────────────

    @Test
    void listByPatientId_returns200_withVaccines() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(vaccineService.listByPatientId(patientId))
                .thenReturn(List.of(buildResponse(UUID.randomUUID(), patientId)));

        mockMvc.perform(get("/api/vaccines/pacientes/{patientId}/historico", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()));
    }

    @Test
    void listByPatientId_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(vaccineService.listByPatientId(patientId)).thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(get("/api/vaccines/pacientes/{patientId}/historico", patientId))
                .andExpect(status().isNotFound());
    }

    // ── UPLOAD PROOF ──────────────────────────────────────────────────────────

    @Test
    void uploadProof_returns201_whenFileIsValid() throws Exception {
        UUID vaccineId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "comprovante.pdf", "application/pdf", "conteudo".getBytes());

        when(vaccineService.uploadProof(eq(vaccineId), any())).thenReturn(
                new VaccineAttachmentResponse(UUID.randomUUID(), vaccineId,
                        "comprovante.pdf", "application/pdf", 8L, AttachmentType.PDF));

        mockMvc.perform(multipart("/api/vaccines/{vaccineId}/comprovante", vaccineId).file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vaccineId").value(vaccineId.toString()))
                .andExpect(jsonPath("$.fileName").value("comprovante.pdf"))
                .andExpect(jsonPath("$.type").value("PDF"));
    }

    @Test
    void uploadProof_returns400_whenFileTypeIsInvalid() throws Exception {
        UUID vaccineId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "c.txt", "text/plain", "texto".getBytes());

        when(vaccineService.uploadProof(eq(vaccineId), any()))
                .thenThrow(new InvalidAttachmentException("Tipo de arquivo não suportado. Use PDF, JPG ou PNG"));

        mockMvc.perform(multipart("/api/vaccines/{vaccineId}/comprovante", vaccineId).file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadProof_returns404_whenVaccineDoesNotExist() throws Exception {
        UUID vaccineId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "c.pdf", "application/pdf", "x".getBytes());

        when(vaccineService.uploadProof(eq(vaccineId), any()))
                .thenThrow(new VaccineNotFoundException(vaccineId));

        mockMvc.perform(multipart("/api/vaccines/{vaccineId}/comprovante", vaccineId).file(file))
                .andExpect(status().isNotFound());
    }

    // ── DOWNLOAD PROOF ────────────────────────────────────────────────────────

    @Test
    void downloadProof_returns200_withFileContent() throws Exception {
        UUID vaccineId = UUID.randomUUID();
        VaccineAttachment attachment = new VaccineAttachment();
        attachment.setFileName("comprovante.pdf");
        attachment.setContentType("application/pdf");
        attachment.setData("conteudo-pdf".getBytes());

        when(vaccineService.getProof(vaccineId)).thenReturn(attachment);

        mockMvc.perform(get("/api/vaccines/{vaccineId}/comprovante", vaccineId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("comprovante.pdf")))
                .andExpect(content().bytes("conteudo-pdf".getBytes()));
    }

    @Test
    void downloadProof_returns404_whenAttachmentDoesNotExist() throws Exception {
        UUID vaccineId = UUID.randomUUID();
        when(vaccineService.getProof(vaccineId)).thenThrow(new VaccineAttachmentNotFoundException(vaccineId));

        mockMvc.perform(get("/api/vaccines/{vaccineId}/comprovante", vaccineId))
                .andExpect(status().isNotFound());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private VaccineRequest buildRequest(UUID patientId) {
        VaccineRequest r = new VaccineRequest();
        r.setPatientId(patientId);
        r.setName("COVID-19");
        r.setApplicationDate(LocalDate.of(2026, 1, 10));
        r.setDose(Dose.FIRST);
        return r;
    }

    private VaccineResponse buildResponse(UUID id, UUID patientId) {
        return new VaccineResponse(id, patientId, "COVID-19", "Pfizer", "ABC123",
                LocalDate.of(2026, 1, 10), Dose.FIRST, null, null, false);
    }
}
