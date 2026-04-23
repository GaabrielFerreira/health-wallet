package com.healthwallet.dto;

import com.healthwallet.model.Dose;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class VaccineResponse {

    private UUID id;
    private UUID patientId;
    private String name;
    private String manufacturer;
    private String lot;
    private LocalDate applicationDate;
    private Dose dose;
    private String proof;
    private String observations;
}
