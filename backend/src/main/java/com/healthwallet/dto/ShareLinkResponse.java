package com.healthwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ShareLinkResponse {

    private UUID token;
    private LocalDateTime expiresAt;
}
