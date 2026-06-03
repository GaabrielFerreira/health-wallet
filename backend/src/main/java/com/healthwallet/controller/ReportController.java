package com.healthwallet.controller;

import com.healthwallet.dto.ReportRequest;
import com.healthwallet.dto.ReportResponse;
import com.healthwallet.model.ReportStatus;
import com.healthwallet.model.ReportType;
import com.healthwallet.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Geração de relatórios de saúde por estratégia")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Gerar relatório de saúde",
        description = "Gera um relatório do paciente conforme o tipo solicitado (FULL, ANAMNESIS, VACCINES, APPOINTMENTS)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Relatório gerado com sucesso",
            content = @Content(schema = @Schema(implementation = ReportResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @PostMapping
    public ResponseEntity<ReportResponse> generate(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.generate(request));
    }

    @Operation(summary = "Listar e filtrar relatórios do paciente",
        description = "Retorna os relatórios de um paciente com filtros opcionais por tipo, status e período de geração")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/pacientes/{patientId}")
    public ResponseEntity<List<ReportResponse>> listByPatient(
            @PathVariable UUID patientId,
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.listByPatientId(patientId, type, status, startDate, endDate));
    }

    @Operation(summary = "Exportar relatório em PDF",
        description = "Gera e retorna o arquivo PDF de um relatório já gerado, identificado pelo seu ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Relatório não encontrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/{reportId}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID reportId) {
        byte[] pdf = reportService.exportPdf(reportId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("relatorio-" + reportId + ".pdf").build().toString())
                .body(pdf);
    }
}
