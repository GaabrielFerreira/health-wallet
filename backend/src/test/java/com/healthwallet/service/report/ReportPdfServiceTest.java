package com.healthwallet.service.report;

import com.healthwallet.model.Report;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportPdfServiceTest {

    private final ReportPdfService reportPdfService = new ReportPdfService();

    @Test
    void generate_producesValidPdfBytes() {
        Report report = new Report();
        report.setObservations("=== RELATÓRIO DE SAÚDE ===\nLinha 1\nLinha 2");

        byte[] pdf = reportPdfService.generate(report);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF"); // assinatura de arquivo PDF
    }

    @Test
    void generate_handlesNullContent() {
        Report report = new Report();
        report.setObservations(null);

        byte[] pdf = reportPdfService.generate(report);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }
}
