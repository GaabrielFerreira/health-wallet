package com.healthwallet.controller;

import com.healthwallet.dto.AppointmentRequest;
import com.healthwallet.dto.AppointmentResponse;
import com.healthwallet.service.AppointmentService;
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
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Operações de registro e listagem de consultas clínicas")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Registrar consulta", description = "Cria uma nova consulta vinculada ao paciente e profissional responsável")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Consulta criada com sucesso",
            content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou campos obrigatórios ausentes",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @Operation(summary = "Listar consultas do paciente", description = "Retorna todas as consultas vinculadas a um paciente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/pacientes/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> listByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(appointmentService.listByPatientId(patientId));
    }
}
