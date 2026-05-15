package com.healthwallet.service;

import com.healthwallet.dto.AppointmentRequest;
import com.healthwallet.dto.AppointmentResponse;
import com.healthwallet.exception.DoctorNotFoundException;
import com.healthwallet.exception.InvalidDoctorRoleException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.model.Appointment;
import com.healthwallet.model.Role;
import com.healthwallet.model.User;
import com.healthwallet.repository.AppointmentRepository;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.service.validation.DoctorValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorValidator doctorValidator;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void create_savesAppointment_withPatientAndDoctorReferences() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        User patient = buildUser(patientId, Role.PATIENT);
        User doctor = buildUser(doctorId, Role.DOCTOR);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorValidator.validateAndGet(doctorId)).thenReturn(doctor);
        when(appointmentRepository.save(any())).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AppointmentResponse response = appointmentService.create(buildRequest(patientId, doctorId));

        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getDoctorId()).isEqualTo(doctorId);
        assertThat(response.getSpecialty()).isEqualTo("Cardiologia");

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getPatient().getId()).isEqualTo(patientId);
        assertThat(captor.getValue().getDoctor().getId()).isEqualTo(doctorId);
    }

    @Test
    void create_throwsPatientNotFound_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(buildRequest(patientId, UUID.randomUUID())))
                .isInstanceOf(PatientNotFoundException.class);

        verify(appointmentRepository, never()).save(any());
        verify(doctorValidator, never()).validateAndGet(any());
    }

    @Test
    void create_throwsDoctorNotFound_whenDoctorMissing() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        when(userRepository.findById(patientId)).thenReturn(Optional.of(buildUser(patientId, Role.PATIENT)));
        when(doctorValidator.validateAndGet(doctorId)).thenThrow(new DoctorNotFoundException(doctorId));

        assertThatThrownBy(() -> appointmentService.create(buildRequest(patientId, doctorId)))
                .isInstanceOf(DoctorNotFoundException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_throwsInvalidDoctorRole_whenUserIsNotDoctor() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        when(userRepository.findById(patientId)).thenReturn(Optional.of(buildUser(patientId, Role.PATIENT)));
        when(doctorValidator.validateAndGet(doctorId)).thenThrow(new InvalidDoctorRoleException(doctorId));

        assertThatThrownBy(() -> appointmentService.create(buildRequest(patientId, doctorId)))
                .isInstanceOf(InvalidDoctorRoleException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void listByPatientId_returnsList() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        User patient = buildUser(patientId, Role.PATIENT);
        User doctor = buildUser(doctorId, Role.DOCTOR);

        Appointment appointment = new Appointment();
        appointment.setId(UUID.randomUUID());
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDate(LocalDateTime.now().plusDays(1));
        appointment.setSpecialty("Cardiologia");
        appointment.setProfessional("Dr. Silva");

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Appointment>>any()))
                .thenReturn(List.of(appointment));

        List<AppointmentResponse> result = appointmentService.listByPatientId(patientId, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(patientId);
        assertThat(result.get(0).getDoctorId()).isEqualTo(doctorId);
    }

    @Test
    void listByPatientId_throwsPatientNotFound_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.listByPatientId(patientId, null, null, null, null))
                .isInstanceOf(PatientNotFoundException.class);
    }

    private User buildUser(UUID id, Role role) {
        User u = new User();
        u.setId(id);
        u.setName("Teste");
        u.setEmail("teste@email.com");
        u.setRole(role);
        return u;
    }

    private AppointmentRequest buildRequest(UUID patientId, UUID doctorId) {
        AppointmentRequest r = new AppointmentRequest();
        r.setPatientId(patientId);
        r.setDoctorId(doctorId);
        r.setDate(LocalDateTime.now().plusDays(1));
        r.setSpecialty("Cardiologia");
        r.setProfessional("Dr. Silva");
        return r;
    }
}
