package com.healthwallet.dto;

import com.healthwallet.model.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReportRequest {

    @NotNull(message = "ID do paciente é obrigatório")
    private UUID patientId;

    @NotNull(message = "Tipo do relatório é obrigatório")
    private ReportType type;
}
