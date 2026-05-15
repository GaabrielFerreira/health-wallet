package com.healthwallet.exception;

import java.util.UUID;

public class DoctorNotFoundException extends RuntimeException {
    public DoctorNotFoundException(UUID doctorId) {
        super("Médico não encontrado: " + doctorId);
    }
}
