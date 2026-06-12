package com.healthwallet.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RenewAccessRequest {

    @NotNull(message = "Nova data de expiração é obrigatória")
    @Future(message = "A nova data de expiração deve ser no futuro")
    private LocalDateTime expiresAt;
}
