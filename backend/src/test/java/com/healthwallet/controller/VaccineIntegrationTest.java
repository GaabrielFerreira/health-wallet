package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.model.Dose;
import com.healthwallet.model.Role;
import com.healthwallet.model.User;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.repository.VaccineAttachmentRepository;
import com.healthwallet.repository.VaccineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class VaccineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaccineRepository vaccineRepository;

    @Autowired
    private VaccineAttachmentRepository vaccineAttachmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID patientId;

    @BeforeEach
    void setup() {
        vaccineAttachmentRepository.deleteAll();
        vaccineRepository.deleteAll();
        userRepository.deleteAll();

        User patient = new User();
        patient.setName("João Silva");
        patient.setEmail("joao@email.com");
        patient.setCpf("12345678901");
        patient.setPassword(passwordEncoder.encode("senha123"));
        patient.setRole(Role.PATIENT);
        patient.setTermsAccepted(true);
        patient.setEmailConfirmed(true);

        patientId = userRepository.save(patient).getId();
    }

    @Test
    void fluxoCompleto_registrarVacinaAnexarERecuperarComprovante() throws Exception {
        // 1. CRIAR vacina
        VaccineRequest request = new VaccineRequest();
        request.setPatientId(patientId);
        request.setName("COVID-19");
        request.setApplicationDate(LocalDate.of(2026, 1, 10));
        request.setDose(Dose.FIRST);

        MvcResult createResult = mockMvc.perform(post("/api/vaccines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasProof").value(false))
                .andReturn();

        UUID vaccineId = UUID.fromString(
                (String) objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class).get("id"));

        // 2. ANEXAR comprovante
        byte[] content = "conteudo-do-comprovante-pdf".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "comprovante.pdf", "application/pdf", content);

        mockMvc.perform(multipart("/api/vaccines/{vaccineId}/comprovante", vaccineId).file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vaccineId").value(vaccineId.toString()))
                .andExpect(jsonPath("$.fileName").value("comprovante.pdf"))
                .andExpect(jsonPath("$.type").value("PDF"));

        // 3. RECUPERAR comprovante
        mockMvc.perform(get("/api/vaccines/{vaccineId}/comprovante", vaccineId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(content));

        // 4. Histórico marca a vacina como tendo comprovante
        mockMvc.perform(get("/api/vaccines/pacientes/{patientId}/historico", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(vaccineId.toString()))
                .andExpect(jsonPath("$[0].hasProof").value(true));

        // 5. Apenas um comprovante persistido
        assertThat(vaccineAttachmentRepository.findByVaccineId(vaccineId)).isPresent();
    }

    @Test
    void upload_returns400_whenFileTypeIsInvalid() throws Exception {
        UUID vaccineId = createVaccine();

        MockMultipartFile file = new MockMultipartFile(
                "file", "comprovante.txt", "text/plain", "texto".getBytes());

        mockMvc.perform(multipart("/api/vaccines/{vaccineId}/comprovante", vaccineId).file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_returns404_whenVaccineDoesNotExist() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "comprovante.pdf", "application/pdf", "x".getBytes());

        mockMvc.perform(multipart("/api/vaccines/{vaccineId}/comprovante", UUID.randomUUID()).file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    void download_returns404_whenVaccineHasNoAttachment() throws Exception {
        UUID vaccineId = createVaccine();

        mockMvc.perform(get("/api/vaccines/{vaccineId}/comprovante", vaccineId))
                .andExpect(status().isNotFound());
    }

    private UUID createVaccine() throws Exception {
        VaccineRequest request = new VaccineRequest();
        request.setPatientId(patientId);
        request.setName("COVID-19");
        request.setApplicationDate(LocalDate.of(2026, 1, 10));
        request.setDose(Dose.FIRST);

        MvcResult result = mockMvc.perform(post("/api/vaccines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(
                (String) objectMapper.readValue(result.getResponse().getContentAsString(), Map.class).get("id"));
    }
}
