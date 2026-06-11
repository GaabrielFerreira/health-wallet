package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.DoctorAccessResponse;
import com.healthwallet.dto.GrantDoctorAccessRequest;
import com.healthwallet.dto.RenewAccessRequest;
import com.healthwallet.exception.CannotRenewRevokedAccessException;
import com.healthwallet.exception.DoctorAccessAlreadyGrantedException;
import com.healthwallet.exception.DoctorNotFoundException;
import com.healthwallet.exception.InvalidDoctorRoleException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.exception.SharedAccessNotFoundException;
import com.healthwallet.model.AccessStatus;
import com.healthwallet.security.JwtService;
import com.healthwallet.security.SecurityConfig;
import com.healthwallet.security.UserDetailsServiceImpl;
import com.healthwallet.service.DoctorAccessService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorAccessController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class DoctorAccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorAccessService doctorAccessService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    // ── POST ──────────────────────────────────────────────────────────────────

    @Test
    void grant_returns201_whenRequestIsValid() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorAccessResponse response = buildResponse(patientId, doctorId);

        when(doctorAccessService.grantAccess(any())).thenReturn(response);

        mockMvc.perform(post("/api/permissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId, doctorId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.doctorName").value("Dra. Ana"))
                .andExpect(jsonPath("$.revoked").value(false));
    }

    @Test
    void grant_returns400_whenPatientIdIsNull() throws Exception {
        GrantDoctorAccessRequest request = buildRequest(null, UUID.randomUUID());

        mockMvc.perform(post("/api/permissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.patientId").exists());
    }

    @Test
    void grant_returns400_whenDoctorIdIsNull() throws Exception {
        GrantDoctorAccessRequest request = buildRequest(UUID.randomUUID(), null);

        mockMvc.perform(post("/api/permissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.doctorId").exists());
    }

    @Test
    void grant_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(doctorAccessService.grantAccess(any())).thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(post("/api/permissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId, UUID.randomUUID()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void grant_returns404_whenDoctorDoesNotExist() throws Exception {
        UUID doctorId = UUID.randomUUID();
        when(doctorAccessService.grantAccess(any())).thenThrow(new DoctorNotFoundException(doctorId));

        mockMvc.perform(post("/api/permissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(UUID.randomUUID(), doctorId))))
                .andExpect(status().isNotFound());
    }

    @Test
    void grant_returns400_whenUserIsNotDoctor() throws Exception {
        UUID userId = UUID.randomUUID();
        when(doctorAccessService.grantAccess(any())).thenThrow(new InvalidDoctorRoleException(userId));

        mockMvc.perform(post("/api/permissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(UUID.randomUUID(), userId))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void grant_returns409_whenAccessAlreadyGranted() throws Exception {
        UUID doctorId = UUID.randomUUID();
        when(doctorAccessService.grantAccess(any()))
                .thenThrow(new DoctorAccessAlreadyGrantedException(doctorId));

        mockMvc.perform(post("/api/permissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(UUID.randomUUID(), doctorId))))
                .andExpect(status().isConflict());
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    void revoke_returns204_whenAccessExists() throws Exception {
        UUID accessId = UUID.randomUUID();

        mockMvc.perform(delete("/api/permissoes/{accessId}", accessId))
                .andExpect(status().isNoContent());

        verify(doctorAccessService).revokeAccess(accessId);
    }

    @Test
    void revoke_returns404_whenAccessDoesNotExist() throws Exception {
        UUID accessId = UUID.randomUUID();
        doThrow(new SharedAccessNotFoundException(accessId))
                .when(doctorAccessService).revokeAccess(accessId);

        mockMvc.perform(delete("/api/permissoes/{accessId}", accessId))
                .andExpect(status().isNotFound());
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Test
    void listByPatient_returns200_withAccesses() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        when(doctorAccessService.listByPatient(patientId))
                .thenReturn(List.of(buildResponse(patientId, doctorId)));

        mockMvc.perform(get("/api/permissoes/pacientes/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()))
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()));
    }

    @Test
    void listByPatient_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(doctorAccessService.listByPatient(patientId))
                .thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(get("/api/permissoes/pacientes/{patientId}", patientId))
                .andExpect(status().isNotFound());
    }

    @Test
    void listByDoctor_returns200_withAccesses() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        when(doctorAccessService.listActiveByDoctor(doctorId))
                .thenReturn(List.of(buildResponse(patientId, doctorId)));

        mockMvc.perform(get("/api/permissoes/medicos/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()));
    }

    @Test
    void listByDoctor_returns400_whenUserIsNotDoctor() throws Exception {
        UUID userId = UUID.randomUUID();
        when(doctorAccessService.listActiveByDoctor(userId))
                .thenThrow(new InvalidDoctorRoleException(userId));

        mockMvc.perform(get("/api/permissoes/medicos/{doctorId}", userId))
                .andExpect(status().isBadRequest());
    }

    // ── PATCH renovar ───────────────────────────────────────────────────────────

    @Test
    void renew_returns200_whenAccessIsRenewed() throws Exception {
        UUID accessId = UUID.randomUUID();
        DoctorAccessResponse response = buildResponse(UUID.randomUUID(), UUID.randomUUID());

        when(doctorAccessService.renewAccess(eq(accessId), any())).thenReturn(response);

        mockMvc.perform(patch("/api/permissoes/{accessId}/renovar", accessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRenewRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void renew_returns400_whenExpiresAtIsNull() throws Exception {
        UUID accessId = UUID.randomUUID();
        RenewAccessRequest request = new RenewAccessRequest();

        mockMvc.perform(patch("/api/permissoes/{accessId}/renovar", accessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.expiresAt").exists());
    }

    @Test
    void renew_returns400_whenAccessIsRevoked() throws Exception {
        UUID accessId = UUID.randomUUID();
        when(doctorAccessService.renewAccess(eq(accessId), any()))
                .thenThrow(new CannotRenewRevokedAccessException(accessId));

        mockMvc.perform(patch("/api/permissoes/{accessId}/renovar", accessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRenewRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void renew_returns404_whenAccessDoesNotExist() throws Exception {
        UUID accessId = UUID.randomUUID();
        when(doctorAccessService.renewAccess(eq(accessId), any()))
                .thenThrow(new SharedAccessNotFoundException(accessId));

        mockMvc.perform(patch("/api/permissoes/{accessId}/renovar", accessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRenewRequest())))
                .andExpect(status().isNotFound());
    }

    // ── GET status ──────────────────────────────────────────────────────────────

    @Test
    void getStatus_returns200_withStatus() throws Exception {
        UUID accessId = UUID.randomUUID();
        DoctorAccessResponse response = buildResponse(UUID.randomUUID(), UUID.randomUUID());

        when(doctorAccessService.getById(accessId)).thenReturn(response);

        mockMvc.perform(get("/api/permissoes/{accessId}", accessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getStatus_returns404_whenAccessDoesNotExist() throws Exception {
        UUID accessId = UUID.randomUUID();
        when(doctorAccessService.getById(accessId))
                .thenThrow(new SharedAccessNotFoundException(accessId));

        mockMvc.perform(get("/api/permissoes/{accessId}", accessId))
                .andExpect(status().isNotFound());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private GrantDoctorAccessRequest buildRequest(UUID patientId, UUID doctorId) {
        GrantDoctorAccessRequest r = new GrantDoctorAccessRequest();
        r.setPatientId(patientId);
        r.setDoctorId(doctorId);
        r.setDataTypes("ANAMNESIS,VACCINES");
        r.setExpiresAt(LocalDateTime.now().plusDays(30));
        return r;
    }

    private RenewAccessRequest buildRenewRequest() {
        RenewAccessRequest r = new RenewAccessRequest();
        r.setExpiresAt(LocalDateTime.now().plusDays(60));
        return r;
    }

    private DoctorAccessResponse buildResponse(UUID patientId, UUID doctorId) {
        return new DoctorAccessResponse(
                UUID.randomUUID(),
                patientId,
                doctorId,
                "Dra. Ana",
                "ana@email.com",
                "ANAMNESIS,VACCINES",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30),
                false,
                AccessStatus.ACTIVE
        );
    }
}
