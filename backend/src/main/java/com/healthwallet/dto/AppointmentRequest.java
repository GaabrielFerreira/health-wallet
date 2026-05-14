package com.healthwallet.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentRequest {

    @NotNull(message = "ID do paciente é obrigatório")
    private UUID patientId;

    @NotNull(message = "Data da consulta é obrigatória")
    @Future(message = "A data da consulta deve ser no futuro")
    private LocalDateTime date;

    @NotBlank(message = "Especialidade é obrigatória")
    private String specialty;

    @NotBlank(message = "Profissional é obrigatório")
    private String professional;

    private String clinic;
    private String summary;
    private String prescription;
    private String medicalObservation;
}
