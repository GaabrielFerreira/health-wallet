package com.healthwallet.controller;

import com.healthwallet.dto.GenerateShareLinkRequest;
import com.healthwallet.dto.PatientDataResponse;
import com.healthwallet.dto.ShareLinkResponse;
import com.healthwallet.service.ShareLinkService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
@Tag(name = "Compartilhamento por link", description = "Geração e consumo de links temporários de compartilhamento de dados")
public class ShareLinkController {

    private final ShareLinkService shareLinkService;

    @Operation(
        summary = "Gerar link de compartilhamento",
        description = "Paciente autenticado gera um token temporário para compartilhar seus dados. Validade padrão: 24h."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Link gerado com sucesso",
            content = @Content(schema = @Schema(implementation = ShareLinkResponse.class))),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @PostMapping("/generate")
    public ResponseEntity<ShareLinkResponse> generate(@Valid @RequestBody GenerateShareLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shareLinkService.generateLink(request));
    }

    @Operation(
        summary = "Acessar dados via token",
        description = "Endpoint público. Qualquer portador do token válido (não expirado e não revogado) acessa os dados consolidados do paciente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso",
            content = @Content(schema = @Schema(implementation = PatientDataResponse.class))),
        @ApiResponse(responseCode = "403", description = "Token inválido, expirado ou revogado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/{token}")
    public ResponseEntity<PatientDataResponse> getByToken(@PathVariable UUID token) {
        return ResponseEntity.ok(shareLinkService.getDataByToken(token));
    }
}
