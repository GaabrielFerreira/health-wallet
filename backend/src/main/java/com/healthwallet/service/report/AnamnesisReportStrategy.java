package com.healthwallet.service.report;

import com.healthwallet.model.ReportType;
import com.healthwallet.model.User;
import com.healthwallet.repository.AnamnesisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnamnesisReportStrategy implements ReportGenerationStrategy {

    private final AnamnesisRepository anamnesisRepository;

    @Override
    public ReportType supports() {
        return ReportType.ANAMNESIS;
    }

    @Override
    public String generate(User patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RELATÓRIO DE ANAMNESE ===\n");
        sb.append("Paciente: ").append(patient.getName()).append("\n\n");

        anamnesisRepository.findByPatientId(patient.getId()).ifPresentOrElse(a -> {
            sb.append("Tipo Sanguíneo: ").append(or(a.getBloodType())).append("\n");
            sb.append("Peso: ").append(a.getWeight() == null ? "-" : a.getWeight() + " kg").append("\n");
            sb.append("Altura: ").append(a.getHeight() == null ? "-" : a.getHeight() + " cm").append("\n");
            sb.append("Alergias: ").append(or(a.getAllergies())).append("\n");
            sb.append("Doenças crônicas: ").append(or(a.getChronicDiseases())).append("\n");
            sb.append("Medicamentos em uso: ").append(or(a.getMedications())).append("\n");
            sb.append("Cirurgias anteriores: ").append(or(a.getPreviousSurgeries())).append("\n");
            sb.append("Histórico familiar: ").append(or(a.getFamilyHistory())).append("\n");
            sb.append("Fumante: ").append(Boolean.TRUE.equals(a.getSmoker()) ? "Sim" : "Não").append("\n");
            sb.append("Atividade física: ").append(or(a.getPhysicalActivity())).append("\n");
            sb.append("Consumo de álcool: ").append(or(a.getAlcoholConsumption())).append("\n");
            sb.append("Observações: ").append(or(a.getObservations())).append("\n");
        }, () -> sb.append("Nenhuma anamnese registrada.\n"));

        return sb.toString();
    }

    private String or(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
