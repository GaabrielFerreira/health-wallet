package com.healthwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DoctorAccessResponse {

    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private String doctorName;
    private String doctorEmail;
    private String dataTypes;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private Boolean revoked;
}
