package com.healthwallet.exception;

import java.util.UUID;

public class AnamnesisNotFoundException extends RuntimeException {
    public AnamnesisNotFoundException(UUID patientId) {
        super("Anamnese não encontrada para o paciente: " + patientId);
    }
}
