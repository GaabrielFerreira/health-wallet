package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.AppointmentRequest;
import com.healthwallet.dto.AppointmentResponse;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.security.SecurityConfig;
import com.healthwallet.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@Import(SecurityConfig.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    // ── POST ──────────────────────────────────────────────────────────────────

    @Test
    void create_returns201_withAppointmentLinkedToPatient() throws Exception {
        UUID patientId = UUID.randomUUID();
        AppointmentResponse response = buildResponse(UUID.randomUUID(), patientId);

        when(appointmentService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.specialty").value("Cardiologia"))
                .andExpect(jsonPath("$.professional").value("Dr. Silva"));
    }

    @Test
    void create_returns400_whenPatientIdIsNull() throws Exception {
        AppointmentRequest request = buildRequest(null);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.patientId").exists());
    }

    @Test
    void create_returns400_whenSpecialtyIsBlank() throws Exception {
        AppointmentRequest request = buildRequest(UUID.randomUUID());
        request.setSpecialty("");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.specialty").exists());
    }

    @Test
    void create_returns400_whenProfessionalIsBlank() throws Exception {
        AppointmentRequest request = buildRequest(UUID.randomUUID());
        request.setProfessional("");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.professional").exists());
    }

    @Test
    void create_returns400_whenDateIsNull() throws Exception {
        AppointmentRequest request = buildRequest(UUID.randomUUID());
        request.setDate(null);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.date").exists());
    }

    @Test
    void create_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(appointmentService.create(any())).thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId))))
                .andExpect(status().isNotFound());
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Test
    void listByPatient_returns200_withAppointments() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(appointmentService.listByPatientId(patientId))
                .thenReturn(List.of(buildResponse(UUID.randomUUID(), patientId)));

        mockMvc.perform(get("/api/appointments/pacientes/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()))
                .andExpect(jsonPath("$[0].specialty").value("Cardiologia"));
    }

    @Test
    void listByPatient_returns200_withEmptyList() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(appointmentService.listByPatientId(patientId)).thenReturn(List.of());

        mockMvc.perform(get("/api/appointments/pacientes/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listByPatient_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(appointmentService.listByPatientId(eq(patientId)))
                .thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(get("/api/appointments/pacientes/{patientId}", patientId))
                .andExpect(status().isNotFound());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private AppointmentRequest buildRequest(UUID patientId) {
        AppointmentRequest r = new AppointmentRequest();
        r.setPatientId(patientId);
        r.setDate(LocalDateTime.now().plusDays(1));
        r.setSpecialty("Cardiologia");
        r.setProfessional("Dr. Silva");
        r.setClinic("Clínica Central");
        r.setSummary("Consulta de rotina");
        r.setPrescription("Losartana 50mg");
        r.setMedicalObservation("Paciente estável");
        return r;
    }

    private AppointmentResponse buildResponse(UUID id, UUID patientId) {
        return new AppointmentResponse(
                id, patientId,
                LocalDateTime.now().plusDays(1),
                "Cardiologia", "Dr. Silva",
                "Clínica Central", "Consulta de rotina",
                "Losartana 50mg", "Paciente estável"
        );
    }
}
