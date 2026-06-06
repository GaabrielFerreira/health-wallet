package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.AppointmentRequest;
import com.healthwallet.model.Role;
import com.healthwallet.model.User;
import com.healthwallet.repository.AppointmentRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID patientId;
    private UUID doctorId;

    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
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

        User doctor = new User();
        doctor.setName("Dra. Ana Lima");
        doctor.setEmail("ana@clinica.com");
        doctor.setCpf("98765432100");
        doctor.setPassword(passwordEncoder.encode("senha123"));
        doctor.setRole(Role.DOCTOR);
        doctor.setTermsAccepted(true);
        doctor.setEmailConfirmed(true);
        doctorId = userRepository.save(doctor).getId();
    }

    // ── CRIAÇÃO ───────────────────────────────────────────────────────────────

    @Test
    void criar_consulta_vinculada_ao_paciente_e_medico() throws Exception {
        AppointmentRequest request = buildRequest(patientId, doctorId);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.specialty").value("Cardiologia"))
                .andExpect(jsonPath("$.professional").value("Dra. Ana Lima"));

        assertThat(appointmentRepository.findAll()).hasSize(1);
        assertThat(appointmentRepository.findAll().get(0).getDoctor().getId()).isEqualTo(doctorId);
        assertThat(appointmentRepository.findAll().get(0).getPatient().getId()).isEqualTo(patientId);
    }

    @Test
    void criar_consulta_retorna_404_quando_paciente_nao_existe() throws Exception {
        AppointmentRequest request = buildRequest(UUID.randomUUID(), doctorId);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertThat(appointmentRepository.findAll()).isEmpty();
    }

    @Test
    void criar_consulta_retorna_404_quando_medico_nao_existe() throws Exception {
        AppointmentRequest request = buildRequest(patientId, UUID.randomUUID());

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertThat(appointmentRepository.findAll()).isEmpty();
    }

    @Test
    void criar_consulta_retorna_400_quando_usuario_nao_e_medico() throws Exception {
        // paciente tentando ser vinculado como médico
        AppointmentRequest request = buildRequest(patientId, patientId);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());

        assertThat(appointmentRepository.findAll()).isEmpty();
    }

    // ── LISTAGEM ──────────────────────────────────────────────────────────────

    @Test
    void listar_consultas_do_paciente_com_dados_reais() throws Exception {
        // Cria duas consultas
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId, doctorId))))
                .andExpect(status().isCreated());

        AppointmentRequest second = buildRequest(patientId, doctorId);
        second.setSpecialty("Neurologia");
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated());

        // Lista sem filtros
        mockMvc.perform(get("/api/appointments/pacientes/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()));
    }

    @Test
    void listar_consultas_com_filtro_de_especialidade() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId, doctorId))))
                .andExpect(status().isCreated());

        AppointmentRequest neuro = buildRequest(patientId, doctorId);
        neuro.setSpecialty("Neurologia");
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(neuro)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/appointments/pacientes/{patientId}", patientId)
                        .param("specialty", "Neurologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].specialty").value("Neurologia"));
    }

    @Test
    void listar_consultas_retorna_404_quando_paciente_nao_existe() throws Exception {
        mockMvc.perform(get("/api/appointments/pacientes/{patientId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private AppointmentRequest buildRequest(UUID patientId, UUID doctorId) {
        AppointmentRequest r = new AppointmentRequest();
        r.setPatientId(patientId);
        r.setDoctorId(doctorId);
        r.setDate(LocalDateTime.now().plusDays(1));
        r.setSpecialty("Cardiologia");
        r.setProfessional("Dra. Ana Lima");
        r.setClinic("Clínica Central");
        r.setSummary("Consulta de rotina");
        return r;
    }
}
