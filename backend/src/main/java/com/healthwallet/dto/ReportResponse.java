package com.healthwallet.dto;

import com.healthwallet.model.ReportStatus;
import com.healthwallet.model.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ReportResponse {

    private UUID id;
    private UUID patientId;
    private ReportType type;
    private ReportStatus status;
    private LocalDateTime generatedAt;
    private String content;
}
