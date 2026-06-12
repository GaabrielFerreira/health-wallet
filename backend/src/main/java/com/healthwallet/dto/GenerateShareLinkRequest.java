package com.healthwallet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class GenerateShareLinkRequest {

    @NotNull(message = "ID do paciente é obrigatório")
    private UUID patientId;

    @Min(value = 1, message = "Validade mínima é 1 hora")
    private int expiresInHours = 24;
}
