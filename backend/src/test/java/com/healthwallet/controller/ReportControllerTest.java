package com.healthwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthwallet.dto.ReportRequest;
import com.healthwallet.dto.ReportResponse;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.exception.ReportNotFoundException;
import com.healthwallet.model.ReportStatus;
import com.healthwallet.model.ReportType;
import com.healthwallet.security.JwtService;
import com.healthwallet.security.UserDetailsServiceImpl;
import com.healthwallet.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    // ── GENERATE ──────────────────────────────────────────────────────────────

    @Test
    void generate_returns201_withReport() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        when(reportService.generate(any())).thenReturn(
                new ReportResponse(reportId, patientId, ReportType.FULL, ReportStatus.COMPLETED,
                        LocalDateTime.now(), "=== RELATÓRIO COMPLETO ==="));

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId, ReportType.FULL))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.type").value("FULL"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void generate_returns400_whenTypeIsMissing() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(UUID.randomUUID(), null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(reportService.generate(any())).thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(patientId, ReportType.ANAMNESIS))))
                .andExpect(status().isNotFound());
    }

    // ── LIST / FILTER ─────────────────────────────────────────────────────────

    @Test
    void listByPatient_returns200_withReports() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(reportService.listByPatientId(eq(patientId), any(), any(), any(), any()))
                .thenReturn(List.of(new ReportResponse(UUID.randomUUID(), patientId,
                        ReportType.VACCINES, ReportStatus.COMPLETED, LocalDateTime.now(), "conteúdo")));

        mockMvc.perform(get("/api/reports/pacientes/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()))
                .andExpect(jsonPath("$[0].type").value("VACCINES"));
    }

    @Test
    void listByPatient_returns404_whenPatientDoesNotExist() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(reportService.listByPatientId(eq(patientId), any(), any(), any(), any()))
                .thenThrow(new PatientNotFoundException(patientId));

        mockMvc.perform(get("/api/reports/pacientes/{patientId}", patientId))
                .andExpect(status().isNotFound());
    }

    // ── EXPORT PDF ────────────────────────────────────────────────────────────

    @Test
    void exportPdf_returns200_withPdfContent() throws Exception {
        UUID reportId = UUID.randomUUID();
        when(reportService.exportPdf(reportId)).thenReturn("%PDF-1.4 conteudo".getBytes());

        mockMvc.perform(get("/api/reports/{reportId}/pdf", reportId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".pdf")));
    }

    @Test
    void exportPdf_returns404_whenReportDoesNotExist() throws Exception {
        UUID reportId = UUID.randomUUID();
        when(reportService.exportPdf(reportId)).thenThrow(new ReportNotFoundException(reportId));

        mockMvc.perform(get("/api/reports/{reportId}/pdf", reportId))
                .andExpect(status().isNotFound());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private ReportRequest buildRequest(UUID patientId, ReportType type) {
        ReportRequest request = new ReportRequest();
        request.setPatientId(patientId);
        request.setType(type);
        return request;
    }
}
