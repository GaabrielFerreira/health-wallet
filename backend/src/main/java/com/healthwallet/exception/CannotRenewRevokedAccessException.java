package com.healthwallet.exception;

import java.util.UUID;

public class CannotRenewRevokedAccessException extends RuntimeException {
    public CannotRenewRevokedAccessException(UUID accessId) {
        super("Não é possível renovar uma permissão revogada: " + accessId);
    }
}
