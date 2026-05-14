package com.healthwallet.exception;

import java.util.UUID;

public class VaccineAttachmentNotFoundException extends RuntimeException {
    public VaccineAttachmentNotFoundException(UUID vaccineId) {
        super("Comprovante não encontrado para a vacina: " + vaccineId);
    }
}
