package com.healthwallet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AnamnesisRequest {

    @NotNull(message = "ID do paciente é obrigatório")
    private UUID patientId;

    private String allergies;

    private String chronicDiseases;

    private String medications;

    private String bloodType;

    private String familyHistory;

    private String observations;

    private Double weight;

    private Double height;

    private String previousSurgeries;

    private Boolean smoker;

    private String physicalActivity;

    private String alcoholConsumption;
}
