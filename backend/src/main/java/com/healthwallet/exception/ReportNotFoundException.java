package com.healthwallet.exception;

import java.util.UUID;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(UUID reportId) {
        super("Relatório não encontrado: " + reportId);
    }
}
