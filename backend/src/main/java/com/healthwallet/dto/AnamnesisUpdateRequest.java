package com.healthwallet.dto;

import lombok.Data;

@Data
public class AnamnesisUpdateRequest {

    private String allergies;
    private String chronicDiseases;
    private String medications;
    private String bloodType;
    private String familyHistory;
    private String observations;
}
