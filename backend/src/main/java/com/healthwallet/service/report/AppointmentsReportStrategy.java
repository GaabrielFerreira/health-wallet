package com.healthwallet.service.report;

import com.healthwallet.model.ReportType;
import com.healthwallet.model.User;
import com.healthwallet.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentsReportStrategy implements ReportGenerationStrategy {

    private final AppointmentRepository appointmentRepository;

    @Override
    public ReportType supports() {
        return ReportType.APPOINTMENTS;
    }

    @Override
    public String generate(User patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RELATÓRIO DE CONSULTAS ===\n");
        sb.append("Paciente: ").append(patient.getName()).append("\n\n");

        var appointments = appointmentRepository.findAllByPatientId(patient.getId());
        if (appointments.isEmpty()) {
            sb.append("Nenhuma consulta registrada.\n");
            return sb.toString();
        }

        appointments.forEach(a -> {
            sb.append("- ").append(a.getDate())
              .append(" | Especialidade: ").append(a.getSpecialty())
              .append(" | Profissional: ").append(a.getProfessional())
              .append(" | Clínica: ").append(a.getClinic() == null ? "-" : a.getClinic())
              .append("\n");
            if (a.getSummary() != null && !a.getSummary().isBlank()) {
                sb.append("  Resumo: ").append(a.getSummary()).append("\n");
            }
        });

        return sb.toString();
    }
}
