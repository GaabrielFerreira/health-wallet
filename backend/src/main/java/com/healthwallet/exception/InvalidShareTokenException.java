package com.healthwallet.exception;

import java.util.UUID;

public class InvalidShareTokenException extends RuntimeException {
    public InvalidShareTokenException(UUID token) {
        super("Token de compartilhamento inválido, expirado ou revogado: " + token);
    }
}
