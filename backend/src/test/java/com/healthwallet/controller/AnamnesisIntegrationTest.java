package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.AnamnesisRequest;
import com.healthwallet.dto.AnamnesisUpdateRequest;
import com.healthwallet.model.Role;
import com.healthwallet.model.User;
import com.healthwallet.repository.AnamnesisRepository;
import com.healthwallet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AnamnesisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnamnesisRepository anamnesisRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID patientId;

    @BeforeEach
    void setup() {
        anamnesisRepository.deleteAll();
        userRepository.deleteAll();

        User patient = new User();
        patient.setName("João Silva");
        patient.setEmail("joao@email.com");
        patient.setCpf("12345678901");
        patient.setPassword(passwordEncoder.encode("senha123"));
        patient.setRole(Role.PATIENT);
        patient.setTermsAccepted(true);
        patient.setEmailConfirmed(true);

        User saved = userRepository.save(patient);
        patientId = saved.getId();
    }

    @Test
    void fluxoCompleto_criarBuscarEditar() throws Exception {
        // 1. CRIAR anamnese vinculada ao paciente
        AnamnesisRequest createRequest = new AnamnesisRequest();
        createRequest.setPatientId(patientId);
        createRequest.setAllergies("Dipirona");
        createRequest.setChronicDiseases("Hipertensão");
        createRequest.setMedications("Losartana 50mg");
        createRequest.setBloodType("O+");
        createRequest.setFamilyHistory("Pai diabético");
        createRequest.setObservations("Nenhuma");

        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.allergies").value("Dipirona"));

        // 2. BUSCAR anamnese do paciente
        mockMvc.perform(get("/api/anamnesis/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.bloodType").value("O+"));

        // 3. EDITAR anamnese
        AnamnesisUpdateRequest updateRequest = new AnamnesisUpdateRequest();
        updateRequest.setAllergies("Amoxicilina");
        updateRequest.setChronicDiseases("Diabetes tipo 2");
        updateRequest.setMedications("Metformina");
        updateRequest.setBloodType("A+");
        updateRequest.setFamilyHistory("Mãe diabética");
        updateRequest.setObservations("Revisão anual");

        mockMvc.perform(put("/api/anamnesis/{patientId}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergies").value("Amoxicilina"))
                .andExpect(jsonPath("$.chronicDiseases").value("Diabetes tipo 2"))
                .andExpect(jsonPath("$.bloodType").value("A+"));

        // 4. Verificar listagem vinculada ao paciente
        mockMvc.perform(get("/api/anamnesis/pacientes/{patientId}/anamneses", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()));

        // 5. Confirmar que só existe 1 anamnese no banco (vínculo correto)
        assertThat(anamnesisRepository.findAllByPatientId(patientId)).hasSize(1);
    }

    @Test
    void create_returns404_whenPatientDoesNotExist() throws Exception {
        AnamnesisRequest request = new AnamnesisRequest();
        request.setPatientId(UUID.randomUUID());
        request.setAllergies("Dipirona");

        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns409_whenAnamnesisAlreadyExists() throws Exception {
        AnamnesisRequest request = new AnamnesisRequest();
        request.setPatientId(patientId);
        request.setAllergies("Dipirona");

        // Primeira criação — sucesso
        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segunda criação — conflito
        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
