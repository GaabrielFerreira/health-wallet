package com.healthwallet.service.report;

import com.healthwallet.model.ReportType;
import com.healthwallet.model.User;
import com.healthwallet.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VaccinesReportStrategy implements ReportGenerationStrategy {

    private final VaccineRepository vaccineRepository;

    @Override
    public ReportType supports() {
        return ReportType.VACCINES;
    }

    @Override
    public String generate(User patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RELATÓRIO DE VACINAÇÃO ===\n");
        sb.append("Paciente: ").append(patient.getName()).append("\n\n");

        var vaccines = vaccineRepository.findByPatientId(patient.getId());
        if (vaccines.isEmpty()) {
            sb.append("Nenhuma vacina registrada.\n");
            return sb.toString();
        }

        vaccines.forEach(v -> {
            sb.append("- ").append(v.getName())
              .append(" | Data: ").append(v.getApplicationDate())
              .append(" | Dose: ").append(v.getDose())
              .append(" | Fabricante: ").append(v.getManufacturer() == null ? "-" : v.getManufacturer())
              .append(" | Lote: ").append(v.getLot() == null ? "-" : v.getLot())
              .append("\n");
        });

        return sb.toString();
    }
}
