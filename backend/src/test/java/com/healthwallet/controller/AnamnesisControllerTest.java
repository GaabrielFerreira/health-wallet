package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.AnamnesisRequest;
import com.healthwallet.dto.AnamnesisResponse;
import com.healthwallet.exception.AnamnesisAlreadyExistsException;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void create_returns201_withPatientIdReference() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID anamnesisId = UUID.randomUUID();

        AnamnesisRequest request = buildRequest(patientId);
        AnamnesisResponse response = buildResponse(anamnesisId, patientId);

        when(anamnesisService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(anamnesisId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.allergies").value("Dipirona"));
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

    @Test
    void create_returns400_whenPatientIdIsNull() throws Exception {
        AnamnesisRequest request = buildRequest(null);

        mockMvc.perform(post("/api/anamnesis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listByPatientId_returns200_withAnamnesisLinkedToPatient() throws Exception {
        UUID patientId = UUID.randomUUID();
        AnamnesisResponse response = buildResponse(UUID.randomUUID(), patientId);

        when(anamnesisService.listByPatientId(patientId)).thenReturn(List.of(response));

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

    private AnamnesisRequest buildRequest(UUID patientId) {
        AnamnesisRequest request = new AnamnesisRequest();
        request.setPatientId(patientId);
        request.setAllergies("Dipirona");
        request.setChronicDiseases("Hipertensão");
        request.setMedications("Losartana 50mg");
        request.setBloodType("O+");
        request.setFamilyHistory("Pai diabético");
        request.setObservations("Nenhuma");
        return request;
    }

    private AnamnesisResponse buildResponse(UUID id, UUID patientId) {
        return new AnamnesisResponse(id, patientId, "Dipirona", "Hipertensão", "Losartana 50mg", "O+", "Pai diabético", "Nenhuma");
    }
}
