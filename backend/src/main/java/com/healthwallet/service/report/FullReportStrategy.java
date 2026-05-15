package com.healthwallet.service.report;

import com.healthwallet.model.ReportType;
import com.healthwallet.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Estratégia composta que reaproveita as demais (Composite leve sobre Strategy).
 * Gera um relatório completo combinando anamnese, vacinas e consultas.
 */
@Component
@RequiredArgsConstructor
public class FullReportStrategy implements ReportGenerationStrategy {

    private final AnamnesisReportStrategy anamnesisStrategy;
    private final VaccinesReportStrategy vaccinesStrategy;
    private final AppointmentsReportStrategy appointmentsStrategy;

    @Override
    public ReportType supports() {
        return ReportType.FULL;
    }

    @Override
    public String generate(User patient) {
        return new StringBuilder()
                .append("=== RELATÓRIO COMPLETO DE SAÚDE ===\n\n")
                .append(anamnesisStrategy.generate(patient)).append("\n")
                .append(vaccinesStrategy.generate(patient)).append("\n")
                .append(appointmentsStrategy.generate(patient))
                .toString();
    }
}
