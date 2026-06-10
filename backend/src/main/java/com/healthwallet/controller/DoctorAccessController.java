package com.healthwallet.controller;

import com.healthwallet.dto.DoctorAccessResponse;
import com.healthwallet.dto.GrantDoctorAccessRequest;
import com.healthwallet.service.DoctorAccessService;
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
@RequestMapping("/api/permissoes")
@RequiredArgsConstructor
@Tag(name = "Permissões médicas", description = "Controle de permissões de acesso de médicos aos dados de pacientes")
public class DoctorAccessController {

    private final DoctorAccessService doctorAccessService;

    @Operation(summary = "Conceder acesso a um médico",
        description = "Permite que um paciente conceda permissão de acesso a um médico específico, com possibilidade de expiração")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Acesso concedido com sucesso",
            content = @Content(schema = @Schema(implementation = DoctorAccessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário não possui papel DOCTOR",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "404", description = "Paciente ou médico não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "409", description = "Médico já possui acesso ativo",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @PostMapping
    public ResponseEntity<DoctorAccessResponse> grant(@Valid @RequestBody GrantDoctorAccessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorAccessService.grantAccess(request));
    }

    @Operation(summary = "Revogar acesso de um médico",
        description = "Revoga uma permissão de acesso previamente concedida")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Acesso revogado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Permissão não encontrada",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @DeleteMapping("/{accessId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID accessId) {
        doctorAccessService.revokeAccess(accessId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar permissões concedidas pelo paciente",
        description = "Retorna todas as permissões (ativas e revogadas) que o paciente já concedeu")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/pacientes/{patientId}")
    public ResponseEntity<List<DoctorAccessResponse>> listByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(doctorAccessService.listByPatient(patientId));
    }

    @Operation(summary = "Listar pacientes que concederam acesso ao médico",
        description = "Retorna todas as permissões ativas vinculadas a um médico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Usuário não possui papel DOCTOR",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "404", description = "Médico não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/medicos/{doctorId}")
    public ResponseEntity<List<DoctorAccessResponse>> listByDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorAccessService.listActiveByDoctor(doctorId));
    }
}
