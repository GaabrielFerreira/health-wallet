package com.healthwallet.controller;

import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.service.VaccineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vaccines")
@RequiredArgsConstructor
@Tag(name = "Vacinas")
public class VaccineController {

    private final VaccineService vaccineService;

    @PostMapping
    @Operation(summary = "Registrar vacina aplicada")
    public ResponseEntity<VaccineResponse> create(@Valid @RequestBody VaccineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vaccineService.create(request));
    }
}
