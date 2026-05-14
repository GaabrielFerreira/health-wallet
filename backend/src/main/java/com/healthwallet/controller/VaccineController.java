package com.healthwallet.controller;

import com.healthwallet.dto.VaccineAttachmentResponse;
import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.model.VaccineAttachment;
import com.healthwallet.service.VaccineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/{vaccineId}/comprovante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Anexar comprovante da vacina",
        description = "Faz upload de um arquivo (PDF, JPG ou PNG, até 5MB) vinculado à vacina")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Comprovante anexado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido (tipo ou tamanho)",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "404", description = "Vacina não encontrada",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<VaccineAttachmentResponse> uploadProof(@PathVariable UUID vaccineId,
                                                                 @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vaccineService.uploadProof(vaccineId, file));
    }

    @GetMapping("/{vaccineId}/comprovante")
    @Operation(summary = "Recuperar comprovante da vacina",
        description = "Retorna o arquivo do comprovante anexado à vacina")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comprovante retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Vacina ou comprovante não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<byte[]> downloadProof(@PathVariable UUID vaccineId) {
        VaccineAttachment attachment = vaccineService.getProof(vaccineId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(attachment.getFileName()).build().toString())
                .body(attachment.getData());
    }
}
