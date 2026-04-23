package com.healthwallet.controller;

import com.healthwallet.dto.AnamnesisRequest;
import com.healthwallet.dto.AnamnesisResponse;
import com.healthwallet.service.AnamnesisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anamnesis")
@RequiredArgsConstructor
@Tag(name = "Anamnese", description = "Operações de registro e gerenciamento de anamnese")
public class AnamnesisController {

    private final AnamnesisService anamnesisService;

    @Operation(summary = "Registrar anamnese", description = "Cria uma nova anamnese para um paciente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Anamnese criada com sucesso",
            content = @Content(schema = @Schema(implementation = AnamnesisResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "409", description = "Paciente já possui anamnese cadastrada",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @PostMapping
    public ResponseEntity<AnamnesisResponse> create(@Valid @RequestBody AnamnesisRequest request) {
        AnamnesisResponse response = anamnesisService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
