package com.healthwallet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class GrantDoctorAccessRequest {

    @NotNull(message = "ID do paciente é obrigatório")
    private UUID patientId;

    @NotNull(message = "ID do médico é obrigatório")
    private UUID doctorId;

    private String dataTypes;

    private LocalDateTime expiresAt;
}
