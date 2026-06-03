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
import com.healthwallet.repository.ReportSpecification;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.service.report.ReportGenerationStrategy;
import com.healthwallet.service.report.ReportPdfService;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final List<ReportGenerationStrategy> strategies;
    private final ReportPdfService reportPdfService;
    private final Map<ReportType, ReportGenerationStrategy> strategyByType = new EnumMap<>(ReportType.class);

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         List<ReportGenerationStrategy> strategies,
                         ReportPdfService reportPdfService) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.strategies = strategies;
        this.reportPdfService = reportPdfService;
    }

    @PostConstruct
    void registerStrategies() {
        strategies.forEach(s -> strategyByType.put(s.supports(), s));
    }

    public ReportResponse generate(ReportRequest request) {
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(request.getPatientId()));

        ReportGenerationStrategy strategy = strategyByType.get(request.getType());
        if (strategy == null) {
            throw new IllegalStateException("Nenhuma estratégia disponível para o tipo: " + request.getType());
        }

        String content = strategy.generate(patient);

        Report report = new Report();
        report.setPatient(patient);
        report.setType(request.getType());
        report.setStatus(ReportStatus.COMPLETED);
        report.setGeneratedAt(LocalDateTime.now());
        report.setObservations(content);

        return toResponse(reportRepository.save(report));
    }

    public List<ReportResponse> listByPatientId(UUID patientId, ReportType type, ReportStatus status,
                                                LocalDate startDate, LocalDate endDate) {
        userRepository.findById(Objects.requireNonNull(patientId))
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        Specification<Report> spec = ReportSpecification.byPatientId(patientId);

        if (type != null) {
            spec = spec.and(ReportSpecification.byType(type));
        }
        if (status != null) {
            spec = spec.and(ReportSpecification.byStatus(status));
        }
        if (startDate != null) {
            spec = spec.and(ReportSpecification.fromDate(startDate));
        }
        if (endDate != null) {
            spec = spec.and(ReportSpecification.toDate(endDate));
        }

        return reportRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "generatedAt")).stream()
                .map(this::toResponse)
                .toList();
    }

    public byte[] exportPdf(UUID reportId) {
        Report report = reportRepository.findById(Objects.requireNonNull(reportId))
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        return reportPdfService.generate(report);
    }

    private ReportResponse toResponse(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getPatient().getId(),
                report.getType(),
                report.getStatus(),
                report.getGeneratedAt(),
                report.getObservations()
        );
    }
}
