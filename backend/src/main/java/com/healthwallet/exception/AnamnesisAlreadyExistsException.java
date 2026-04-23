package com.healthwallet.exception;

public class AnamnesisAlreadyExistsException extends RuntimeException {

    public AnamnesisAlreadyExistsException() {
        super("Paciente já possui uma anamnese cadastrada");
    }
}
