package com.healthwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AppointmentResponse {

    private UUID id;
    private UUID patientId;
    private LocalDateTime date;
    private String specialty;
    private String professional;
    private String clinic;
    private String summary;
    private String prescription;
    private String medicalObservation;
}
