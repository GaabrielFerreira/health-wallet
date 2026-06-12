package com.healthwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PatientDataResponse {

    private AnamnesisResponse anamnesis;
    private List<VaccineResponse> vaccines;
    private List<AppointmentResponse> appointments;
}
