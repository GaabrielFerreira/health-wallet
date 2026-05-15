package com.healthwallet.service.report;

import com.healthwallet.model.ReportType;
import com.healthwallet.model.User;

/**
 * Contrato do padrão Strategy para geração de relatórios.
 * Cada implementação produz um tipo específico de relatório (full, anamnese, vacinas, consultas).
 * O ReportService seleciona a estratégia em tempo de execução pelo ReportType.
 */
public interface ReportGenerationStrategy {

    ReportType supports();

    String generate(User patient);
}
