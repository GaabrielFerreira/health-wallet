package com.healthwallet.service;

import com.healthwallet.dto.ReportRequest;
import com.healthwallet.dto.ReportResponse;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.exception.ReportNotFoundException;
import com.healthwallet.model.Report;
import com.healthwallet.model.ReportStatus;
import com.healthwallet.model.ReportType;
import com.healthwallet.model.User;
import com.healthwallet.repository.ReportRepository;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.service.report.ReportGenerationStrategy;
import com.healthwallet.service.report.ReportPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    private ReportRepository reportRepository;
    private UserRepository userRepository;
    private ReportGenerationStrategy anamnesisStrategy;
    private ReportGenerationStrategy vaccinesStrategy;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportRepository = mock(ReportRepository.class);
        userRepository = mock(UserRepository.class);
        anamnesisStrategy = mock(ReportGenerationStrategy.class);
        vaccinesStrategy = mock(ReportGenerationStrategy.class);

        when(anamnesisStrategy.supports()).thenReturn(ReportType.ANAMNESIS);
        when(vaccinesStrategy.supports()).thenReturn(ReportType.VACCINES);

        reportService = new ReportService(reportRepository, userRepository,
                List.of(anamnesisStrategy, vaccinesStrategy), new ReportPdfService());
        reportService.registerStrategies();
    }

    @Test
    void generate_selectsStrategyByType_andSavesReport() {
        UUID patientId = UUID.randomUUID();
        User patient = new User();
        patient.setId(patientId);
        patient.setName("Maria");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(anamnesisStrategy.generate(patient)).thenReturn("conteúdo anamnese");
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> {
            Report r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ReportRequest request = new ReportRequest();
        request.setPatientId(patientId);
        request.setType(ReportType.ANAMNESIS);

        ReportResponse response = reportService.generate(request);

        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getType()).isEqualTo(ReportType.ANAMNESIS);
        assertThat(response.getStatus()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(response.getContent()).isEqualTo("conteúdo anamnese");

        verify(anamnesisStrategy).generate(patient);
        verify(vaccinesStrategy, never()).generate(any());
    }

    @Test
    void generate_usesVaccinesStrategy_whenTypeIsVaccines() {
        UUID patientId = UUID.randomUUID();
        User patient = new User();
        patient.setId(patientId);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(vaccinesStrategy.generate(patient)).thenReturn("conteúdo vacinas");
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportRequest request = new ReportRequest();
        request.setPatientId(patientId);
        request.setType(ReportType.VACCINES);

        ReportResponse response = reportService.generate(request);

        assertThat(response.getContent()).isEqualTo("conteúdo vacinas");
        verify(vaccinesStrategy).generate(patient);
        verify(anamnesisStrategy, never()).generate(any());
    }

    @Test
    void generate_throwsPatientNotFound_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        ReportRequest request = new ReportRequest();
        request.setPatientId(patientId);
        request.setType(ReportType.ANAMNESIS);

        assertThatThrownBy(() -> reportService.generate(request))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void generate_throwsIllegalState_whenNoStrategyForType() {
        UUID patientId = UUID.randomUUID();
        User patient = new User();
        patient.setId(patientId);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        ReportRequest request = new ReportRequest();
        request.setPatientId(patientId);
        request.setType(ReportType.FULL); // não registrado neste teste

        assertThatThrownBy(() -> reportService.generate(request))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listByPatientId_returnsMappedReports() {
        UUID patientId = UUID.randomUUID();
        User patient = new User();
        patient.setId(patientId);
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setPatient(patient);
        report.setType(ReportType.VACCINES);
        report.setStatus(ReportStatus.COMPLETED);
        report.setObservations("conteúdo do relatório");
        when(reportRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(report));

        List<ReportResponse> result = reportService.listByPatientId(patientId, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(patientId);
        assertThat(result.get(0).getType()).isEqualTo(ReportType.VACCINES);
        assertThat(result.get(0).getContent()).isEqualTo("conteúdo do relatório");
    }

    @Test
    void listByPatientId_throwsPatientNotFound_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.listByPatientId(patientId, null, null, null, null))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void exportPdf_returnsPdfBytes() {
        UUID reportId = UUID.randomUUID();
        User patient = new User();
        patient.setId(UUID.randomUUID());

        Report report = new Report();
        report.setId(reportId);
        report.setPatient(patient);
        report.setType(ReportType.FULL);
        report.setObservations("=== RELATÓRIO COMPLETO ===");
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        byte[] pdf = reportService.exportPdf(reportId);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF"); // assinatura de arquivo PDF
    }

    @Test
    void exportPdf_throwsReportNotFound_whenReportMissing() {
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.exportPdf(reportId))
                .isInstanceOf(ReportNotFoundException.class);
    }
}
