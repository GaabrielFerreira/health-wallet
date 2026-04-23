package com.healthwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AnamnesisResponse {

    private UUID id;
    private UUID patientId;
    private String allergies;
    private String chronicDiseases;
    private String medications;
    private String bloodType;
    private String familyHistory;
    private String observations;
}
