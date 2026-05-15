package com.healthwallet.exception;

import java.util.UUID;

public class InvalidDoctorRoleException extends RuntimeException {
    public InvalidDoctorRoleException(UUID userId) {
        super("Usuário " + userId + " não possui o papel DOCTOR e não pode ser vinculado como médico responsável");
    }
}
