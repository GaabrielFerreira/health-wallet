package com.healthwallet.service.report;

import com.healthwallet.model.Dose;
import com.healthwallet.model.ReportType;
import com.healthwallet.model.User;
import com.healthwallet.model.Vaccine;
import com.healthwallet.repository.VaccineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaccinesReportStrategyTest {

    @Mock
    private VaccineRepository vaccineRepository;

    @InjectMocks
    private VaccinesReportStrategy strategy;

    @Test
    void supports_returnsVaccinesType() {
        assertThat(strategy.supports()).isEqualTo(ReportType.VACCINES);
    }

    @Test
    void generate_returnsEmptyMessage_whenPatientHasNoVaccines() {
        User patient = buildPatient();
        when(vaccineRepository.findByPatientId(patient.getId())).thenReturn(List.of());

        String report = strategy.generate(patient);

        assertThat(report)
                .contains("=== RELATÓRIO DE VACINAÇÃO ===")
                .contains("Paciente: João Silva")
                .contains("Nenhuma vacina registrada.");
    }

    @Test
    void generate_listsAllVaccines_withApplicationDateAndDose() {
        User patient = buildPatient();
        Vaccine v1 = buildVaccine(patient, "COVID-19", "Pfizer", "ABC123", Dose.FIRST);
        Vaccine v2 = buildVaccine(patient, "Influenza", "Butantan", "INF999", Dose.BOOSTER);

        when(vaccineRepository.findByPatientId(patient.getId())).thenReturn(List.of(v1, v2));

        String report = strategy.generate(patient);

        assertThat(report)
                .contains("Paciente: João Silva")
                .contains("COVID-19")
                .contains("Pfizer")
                .contains("ABC123")
                .contains("FIRST")
                .contains("Influenza")
                .contains("Butantan")
                .contains("INF999")
                .contains("BOOSTER")
                .contains("2026-01-10");
    }

    @Test
    void generate_replacesNullManufacturerAndLot_withDash() {
        User patient = buildPatient();
        Vaccine v = buildVaccine(patient, "Febre Amarela", null, null, Dose.SECOND);

        when(vaccineRepository.findByPatientId(patient.getId())).thenReturn(List.of(v));

        String report = strategy.generate(patient);

        assertThat(report)
                .contains("Febre Amarela")
                .contains("Fabricante: -")
                .contains("Lote: -");
    }

    private User buildPatient() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setName("João Silva");
        return u;
    }

    private Vaccine buildVaccine(User patient, String name, String manufacturer, String lot, Dose dose) {
        Vaccine v = new Vaccine();
        v.setId(UUID.randomUUID());
        v.setPatient(patient);
        v.setName(name);
        v.setManufacturer(manufacturer);
        v.setLot(lot);
        v.setApplicationDate(LocalDate.of(2026, 1, 10));
        v.setDose(dose);
        return v;
    }
}
