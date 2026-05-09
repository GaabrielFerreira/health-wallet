package com.healthwallet.controller;

import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.service.VaccineService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vaccines")
@RequiredArgsConstructor
@Tag(name = "Vacinas", description = "Operações de registro e gerenciamento de vacinas")
public class VaccineController {

    private final VaccineService vaccineService;

    @PostMapping
    @Operation(summary = "Registrar vacina aplicada")
    public ResponseEntity<VaccineResponse> create(@Valid @RequestBody VaccineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vaccineService.create(request));
    }

    @GetMapping("/pacientes/{patientId}/historico")
    @Operation(summary = "Listar histórico de vacinação", description = "Retorna todas as vacinas registradas para um paciente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<List<VaccineResponse>> listByPatientId(@PathVariable UUID patientId) {
        return ResponseEntity.ok(vaccineService.listByPatientId(patientId));
    }
}
