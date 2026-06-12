package com.healthwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DoctorLookupResponse {

    private UUID id;
    private String name;
    private String email;
}
