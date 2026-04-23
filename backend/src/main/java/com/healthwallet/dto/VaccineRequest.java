package com.healthwallet.dto;

import com.healthwallet.model.Dose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class VaccineRequest {

    @NotNull(message = "ID do paciente é obrigatório")
    private UUID patientId;

    @NotBlank(message = "Nome da vacina é obrigatório")
    private String name;

    private String manufacturer;

    private String lot;

    @NotNull(message = "Data de aplicação é obrigatória")
    private LocalDate applicationDate;

    @NotNull(message = "Dose é obrigatória")
    private Dose dose;

    private String proof;

    private String observations;
}
