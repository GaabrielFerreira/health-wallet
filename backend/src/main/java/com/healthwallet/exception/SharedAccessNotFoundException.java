package com.healthwallet.exception;

import java.util.UUID;

public class SharedAccessNotFoundException extends RuntimeException {
    public SharedAccessNotFoundException(UUID id) {
        super("Permissão de acesso não encontrada: " + id);
    }
}
