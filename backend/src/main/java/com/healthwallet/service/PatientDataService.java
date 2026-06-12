package com.healthwallet.service;

import com.healthwallet.dto.AnamnesisResponse;
import com.healthwallet.dto.AppointmentResponse;
import com.healthwallet.dto.PatientDataResponse;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.exception.AnamnesisNotFoundException;
import com.healthwallet.service.validation.DoctorAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Agrega os dados de saúde de um paciente para visualização por médico autorizado (SCRUM-73).
 *
 * SOLID:
 * - SRP: responsabilidade única de agregar dados sob validação de permissão.
 * - DIP: depende de abstrações (services existentes) em vez de repositórios concretos.
 */
@Service
@RequiredArgsConstructor
public class PatientDataService {

    private final DoctorAccessValidator doctorAccessValidator;
    private final AnamnesisService anamnesisService;
    private final VaccineService vaccineService;
    private final AppointmentService appointmentService;

    public PatientDataResponse getPatientData(UUID doctorId, UUID patientId) {
        doctorAccessValidator.requireActiveAccess(doctorId, patientId);

        AnamnesisResponse anamnesis = null;
        try {
            anamnesis = anamnesisService.getByPatientId(patientId);
        } catch (AnamnesisNotFoundException ignored) {
        }

        List<VaccineResponse> vaccines = vaccineService.listByPatientId(patientId);
        List<AppointmentResponse> appointments = appointmentService.listByPatientId(patientId, null, null, null, null);

        return new PatientDataResponse(anamnesis, vaccines, appointments);
    }
}
