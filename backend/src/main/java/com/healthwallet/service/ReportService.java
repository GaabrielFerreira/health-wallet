package com.healthwallet.service;

import com.healthwallet.dto.ReportRequest;
import com.healthwallet.dto.ReportResponse;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.model.Report;
import com.healthwallet.model.ReportStatus;
import com.healthwallet.model.ReportType;
import com.healthwallet.model.User;
import com.healthwallet.repository.ReportRepository;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.service.report.ReportGenerationStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final List<ReportGenerationStrategy> strategies;
    private final Map<ReportType, ReportGenerationStrategy> strategyByType = new EnumMap<>(ReportType.class);

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         List<ReportGenerationStrategy> strategies) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.strategies = strategies;
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
        Report saved = reportRepository.save(report);

        return new ReportResponse(
                saved.getId(),
                saved.getPatient().getId(),
                saved.getType(),
                saved.getStatus(),
                saved.getGeneratedAt(),
                content
        );
    }
}
