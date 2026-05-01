package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.AnamnesisRequest;
import com.healthwallet.dto.AnamnesisResponse;
import com.healthwallet.dto.AnamnesisUpdateRequest;
import com.healthwallet.exception.AnamnesisAlreadyExistsException;
import com.healthwallet.exception.AnamnesisNotFoundException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.security.SecurityConfig;
import com.healthwallet.service.AnamnesisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnamnesisController.class)
@Import(SecurityConfig.class)
class AnamnesisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnamnesisService anamnesisService;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Test
    void create_returns201_withPatientIdReference() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID anamnesisId = UUID.randomUUID();

        when(anamnesisService.create(any())).thenReturn(buildResponse(anamnesisId, patientId));

        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(anamnesisId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.allergies").value("Dipirona"));
    }

    @Test
    void create_returns400_whenPatientIdIsNull() throws Exception {
        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(anamnesisService.create(any())).thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId))))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns409_whenAnamnesisAlreadyExists() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(anamnesisService.create(any())).thenThrow(new AnamnesisAlreadyExistsException());

        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId))))
                .andExpect(status().isConflict());
    }

    // ── GET BY PATIENT ID ─────────────────────────────────────────────────────

    @Test
    void getByPatientId_returns200_withAnamnesisData() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID anamnesisId = UUID.randomUUID();

        when(anamnesisService.getByPatientId(patientId)).thenReturn(buildResponse(anamnesisId, patientId));

        mockMvc.perform(get("/api/anamnesis/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.bloodType").value("O+"))
                .andExpect(jsonPath("$.medications").value("Losartana 50mg"));
    }

    @Test
    void getByPatientId_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(anamnesisService.getByPatientId(patientId)).thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(get("/api/anamnesis/{patientId}", patientId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByPatientId_returns404_whenAnamnesisNotFound() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(anamnesisService.getByPatientId(patientId)).thenThrow(new AnamnesisNotFoundException(patientId));

        mockMvc.perform(get("/api/anamnesis/{patientId}", patientId))
                .andExpect(status().isNotFound());
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Test
    void update_returns200_withUpdatedData() throws Exception {
        UUID patientId = UUID.randomUUID();
        AnamnesisUpdateRequest updateRequest = buildUpdateRequest("Amoxicilina", "Diabetes tipo 2");
        AnamnesisResponse updated = new AnamnesisResponse(
                UUID.randomUUID(), patientId, "Amoxicilina", "Diabetes tipo 2",
                "Metformina", "A+", "Mãe diabética", "Revisão anual"
        );

        when(anamnesisService.update(eq(patientId), any())).thenReturn(updated);

        mockMvc.perform(put("/api/anamnesis/{patientId}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergies").value("Amoxicilina"))
                .andExpect(jsonPath("$.chronicDiseases").value("Diabetes tipo 2"));
    }

    @Test
    void update_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(anamnesisService.update(eq(patientId), any())).thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(put("/api/anamnesis/{patientId}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateRequest("X", "Y"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_returns404_whenAnamnesisDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(anamnesisService.update(eq(patientId), any())).thenThrow(new AnamnesisNotFoundException(patientId));

        mockMvc.perform(put("/api/anamnesis/{patientId}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUpdateRequest("X", "Y"))))
                .andExpect(status().isNotFound());
    }

    // ── LIST BY PATIENT ───────────────────────────────────────────────────────

    @Test
    void listByPatientId_returns200_withAnamnesisLinkedToPatient() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(anamnesisService.listByPatientId(patientId))
                .thenReturn(List.of(buildResponse(UUID.randomUUID(), patientId)));

        mockMvc.perform(get("/api/anamnesis/pacientes/{patientId}/anamneses", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()))
                .andExpect(jsonPath("$[0].allergies").value("Dipirona"));
    }

    @Test
    void listByPatientId_returns200_withEmptyList_whenNoAnamnesis() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(anamnesisService.listByPatientId(patientId)).thenReturn(List.of());

        mockMvc.perform(get("/api/anamnesis/pacientes/{patientId}/anamneses", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listByPatientId_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(anamnesisService.listByPatientId(patientId)).thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(get("/api/anamnesis/pacientes/{patientId}/anamneses", patientId))
                .andExpect(status().isNotFound());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private AnamnesisRequest buildRequest(UUID patientId) {
        AnamnesisRequest r = new AnamnesisRequest();
        r.setPatientId(patientId);
        r.setAllergies("Dipirona");
        r.setChronicDiseases("Hipertensão");
        r.setMedications("Losartana 50mg");
        r.setBloodType("O+");
        r.setFamilyHistory("Pai diabético");
        r.setObservations("Nenhuma");
        return r;
    }

    private AnamnesisUpdateRequest buildUpdateRequest(String allergies, String chronicDiseases) {
        AnamnesisUpdateRequest r = new AnamnesisUpdateRequest();
        r.setAllergies(allergies);
        r.setChronicDiseases(chronicDiseases);
        r.setMedications("Metformina");
        r.setBloodType("A+");
        r.setFamilyHistory("Mãe diabética");
        r.setObservations("Revisão anual");
        return r;
    }

    private AnamnesisResponse buildResponse(UUID id, UUID patientId) {
        return new AnamnesisResponse(id, patientId, "Dipirona", "Hipertensão",
                "Losartana 50mg", "O+", "Pai diabético", "Nenhuma");
    }
}
