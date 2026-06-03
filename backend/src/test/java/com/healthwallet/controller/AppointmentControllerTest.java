package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.AppointmentRequest;
import com.healthwallet.dto.AppointmentResponse;
import com.healthwallet.exception.DoctorNotFoundException;
import com.healthwallet.exception.InvalidDoctorRoleException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.security.JwtService;
import com.healthwallet.security.SecurityConfig;
import com.healthwallet.security.UserDetailsServiceImpl;
import com.healthwallet.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    // ── POST ──────────────────────────────────────────────────────────────────

    @Test
    void create_returns201_withAppointmentLinkedToPatientAndDoctor() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        AppointmentResponse response = buildResponse(UUID.randomUUID(), patientId, doctorId);

        when(appointmentService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId, doctorId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.specialty").value("Cardiologia"));
    }

    @Test
    void create_returns400_whenPatientIdIsNull() throws Exception {
        AppointmentRequest request = buildRequest(null, UUID.randomUUID());

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.patientId").exists());
    }

    @Test
    void create_returns201_whenDoctorIdIsNull() throws Exception {
        // médico é opcional (doctorId não tem @NotNull): consulta sem médico deve ser criada normalmente
        UUID patientId = UUID.randomUUID();
        when(appointmentService.create(any()))
                .thenReturn(buildResponse(UUID.randomUUID(), patientId, null));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.doctorId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.specialty").value("Cardiologia"));
    }

    @Test
    void create_returns400_whenSpecialtyIsBlank() throws Exception {
        AppointmentRequest request = buildRequest(UUID.randomUUID(), UUID.randomUUID());
        request.setSpecialty("");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.specialty").exists());
    }

    @Test
    void create_returns400_whenProfessionalIsBlank() throws Exception {
        AppointmentRequest request = buildRequest(UUID.randomUUID(), UUID.randomUUID());
        request.setProfessional("");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.professional").exists());
    }

    @Test
    void create_returns400_whenDateIsNull() throws Exception {
        AppointmentRequest request = buildRequest(UUID.randomUUID(), UUID.randomUUID());
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
                        .content(objectMapper.writeValueAsString(buildRequest(patientId, UUID.randomUUID()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns404_whenDoctorDoesNotExist() throws Exception {
        UUID doctorId = UUID.randomUUID();
        when(appointmentService.create(any())).thenThrow(new DoctorNotFoundException(doctorId));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(UUID.randomUUID(), doctorId))))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns400_whenUserIsNotDoctor() throws Exception {
        UUID userId = UUID.randomUUID();
        when(appointmentService.create(any())).thenThrow(new InvalidDoctorRoleException(userId));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(UUID.randomUUID(), userId))))
                .andExpect(status().isBadRequest());
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Test
    void listByPatient_returns200_withAppointments() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        when(appointmentService.listByPatientId(eq(patientId), any(), any(), any(), any()))
                .thenReturn(List.of(buildResponse(UUID.randomUUID(), patientId, doctorId)));

        mockMvc.perform(get("/api/appointments/pacientes/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()))
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$[0].specialty").value("Cardiologia"));
    }

    @Test
    void listByPatient_returns200_withEmptyList() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(appointmentService.listByPatientId(eq(patientId), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/appointments/pacientes/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listByPatient_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(appointmentService.listByPatientId(eq(patientId), any(), any(), any(), any()))
                .thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(get("/api/appointments/pacientes/{patientId}", patientId))
                .andExpect(status().isNotFound());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private AppointmentRequest buildRequest(UUID patientId, UUID doctorId) {
        AppointmentRequest r = new AppointmentRequest();
        r.setPatientId(patientId);
        r.setDoctorId(doctorId);
        r.setDate(LocalDateTime.now().plusDays(1));
        r.setSpecialty("Cardiologia");
        r.setProfessional("Dr. Silva");
        r.setClinic("Clínica Central");
        r.setSummary("Consulta de rotina");
        r.setPrescription("Losartana 50mg");
        r.setMedicalObservation("Paciente estável");
        return r;
    }

    private AppointmentResponse buildResponse(UUID id, UUID patientId, UUID doctorId) {
        return new AppointmentResponse(
                id, patientId, doctorId,
                LocalDateTime.now().plusDays(1),
                "Cardiologia", "Dr. Silva",
                "Clínica Central", "Consulta de rotina",
                "Losartana 50mg", "Paciente estável"
        );
    }
}
