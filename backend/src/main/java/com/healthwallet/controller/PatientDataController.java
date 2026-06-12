package com.healthwallet.controller;

import com.healthwallet.dto.PatientDataResponse;
import com.healthwallet.service.PatientDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/permissoes")
@RequiredArgsConstructor
@Tag(name = "Dados do paciente", description = "Acesso consolidado aos dados de saúde do paciente por médico autorizado")
public class PatientDataController {

    private final PatientDataService patientDataService;

    @Operation(
        summary = "Visualizar dados consolidados do paciente",
        description = "Retorna anamnese, vacinas e consultas do paciente. Exige permissão ativa concedida pelo próprio paciente ao médico."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso",
            content = @Content(schema = @Schema(implementation = PatientDataResponse.class))),
        @ApiResponse(responseCode = "403", description = "Médico não possui permissão ativa para este paciente",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "404", description = "Médico ou paciente não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/medicos/{doctorId}/pacientes/{patientId}/dados")
    public ResponseEntity<PatientDataResponse> getPatientData(
            @PathVariable UUID doctorId,
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(patientDataService.getPatientData(doctorId, patientId));
    }
}
