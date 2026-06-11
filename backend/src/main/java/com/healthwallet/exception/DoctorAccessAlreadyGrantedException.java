package com.healthwallet.exception;

import java.util.UUID;

public class DoctorAccessAlreadyGrantedException extends RuntimeException {
    public DoctorAccessAlreadyGrantedException(UUID doctorId) {
        super("Médico " + doctorId + " já possui acesso ativo aos dados deste paciente");
    }
}
