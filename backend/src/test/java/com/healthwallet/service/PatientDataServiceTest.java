package com.healthwallet.service;

import com.healthwallet.dto.AnamnesisResponse;
import com.healthwallet.dto.PatientDataResponse;
import com.healthwallet.exception.AnamnesisNotFoundException;
import com.healthwallet.exception.UnauthorizedDoctorAccessException;
import com.healthwallet.service.validation.DoctorAccessValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientDataServiceTest {

    @Mock
    private DoctorAccessValidator doctorAccessValidator;

    @Mock
    private AnamnesisService anamnesisService;

    @Mock
    private VaccineService vaccineService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private PatientDataService service;

    @Test
    void getPatientData_returnsAggregatedData_whenDoctorIsAuthorized() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        AnamnesisResponse anamnesis = buildAnamnesis(patientId);
        when(anamnesisService.getByPatientId(patientId)).thenReturn(anamnesis);
        when(vaccineService.listByPatientId(patientId)).thenReturn(List.of());
        when(appointmentService.listByPatientId(patientId, null, null, null, null)).thenReturn(List.of());

        PatientDataResponse data = service.getPatientData(doctorId, patientId);

        assertThat(data.getAnamnesis()).isEqualTo(anamnesis);
        assertThat(data.getVaccines()).isEmpty();
        assertThat(data.getAppointments()).isEmpty();
        verify(doctorAccessValidator).requireActiveAccess(doctorId, patientId);
    }

    @Test
    void getPatientData_throwsUnauthorized_whenDoctorHasNoActiveAccess() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        doThrow(new UnauthorizedDoctorAccessException(doctorId, patientId))
                .when(doctorAccessValidator).requireActiveAccess(doctorId, patientId);

        assertThatThrownBy(() -> service.getPatientData(doctorId, patientId))
                .isInstanceOf(UnauthorizedDoctorAccessException.class);

        verifyNoInteractions(anamnesisService, vaccineService, appointmentService);
    }

    @Test
    void getPatientData_returnsNullAnamnesis_whenAnamnesisMissing() {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        when(anamnesisService.getByPatientId(patientId)).thenThrow(new AnamnesisNotFoundException(patientId));
        when(vaccineService.listByPatientId(patientId)).thenReturn(List.of());
        when(appointmentService.listByPatientId(patientId, null, null, null, null)).thenReturn(List.of());

        PatientDataResponse data = service.getPatientData(doctorId, patientId);

        assertThat(data.getAnamnesis()).isNull();
    }

    private AnamnesisResponse buildAnamnesis(UUID patientId) {
        return new AnamnesisResponse(
                UUID.randomUUID(), patientId, null, null, null, "A+", null, null,
                null, null, null, null, null, null);
    }
}
