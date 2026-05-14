package com.healthwallet.exception;

import java.util.UUID;

public class VaccineNotFoundException extends RuntimeException {
    public VaccineNotFoundException(UUID vaccineId) {
        super("Vacina não encontrada: " + vaccineId);
    }
}
