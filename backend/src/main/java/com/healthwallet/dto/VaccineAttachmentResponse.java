package com.healthwallet.dto;

import com.healthwallet.model.AttachmentType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class VaccineAttachmentResponse {

    private UUID id;
    private UUID vaccineId;
    private String fileName;
    private String contentType;
    private long fileSize;
    private AttachmentType type;
}
