package com.healthwallet.exception;

import java.util.UUID;

public class UnauthorizedDoctorAccessException extends RuntimeException {
    public UnauthorizedDoctorAccessException(UUID doctorId, UUID patientId) {
        super("Médico " + doctorId + " não possui permissão de acesso aos dados do paciente " + patientId);
    }
}
