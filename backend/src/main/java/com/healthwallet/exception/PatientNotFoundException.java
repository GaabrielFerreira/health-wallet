package com.healthwallet.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(UUID patientId) {
        super("Paciente não encontrado: " + patientId);
    }
}
