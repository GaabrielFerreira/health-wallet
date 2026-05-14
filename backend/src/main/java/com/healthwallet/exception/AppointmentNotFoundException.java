package com.healthwallet.exception;

import java.util.UUID;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(UUID id) {
        super("Consulta não encontrada: " + id);
    }
}
