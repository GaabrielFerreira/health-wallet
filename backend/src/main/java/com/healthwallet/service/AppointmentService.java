package com.healthwallet.service;

import com.healthwallet.dto.AppointmentRequest;
import com.healthwallet.dto.AppointmentResponse;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.model.Appointment;
import com.healthwallet.model.User;
import com.healthwallet.repository.AppointmentRepository;
import com.healthwallet.repository.AppointmentSpecification;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.service.validation.DoctorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorValidator doctorValidator;

    public AppointmentResponse create(AppointmentRequest request) {
        UUID patientId = Objects.requireNonNull(request.getPatientId());
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        User doctor = request.getDoctorId() != null ? doctorValidator.validateAndGet(request.getDoctorId()) : null;

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDate(request.getDate());
        appointment.setSpecialty(request.getSpecialty());
        appointment.setProfessional(request.getProfessional());
        appointment.setClinic(request.getClinic());
        appointment.setSummary(request.getSummary());
        appointment.setPrescription(request.getPrescription());
        appointment.setMedicalObservation(request.getMedicalObservation());

        return toResponse(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> listByPatientId(UUID patientId, String specialty, String professional, LocalDate startDate, LocalDate endDate) {
        userRepository.findById(Objects.requireNonNull(patientId))
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        Specification<Appointment> spec = AppointmentSpecification.byPatientId(patientId);

        if (specialty != null && !specialty.isBlank()) {
            spec = spec.and(AppointmentSpecification.bySpecialty(specialty));
        }
        if (professional != null && !professional.isBlank()) {
            spec = spec.and(AppointmentSpecification.byProfessional(professional));
        }
        if (startDate != null) {
            spec = spec.and(AppointmentSpecification.fromDate(startDate));
        }
        if (endDate != null) {
            spec = spec.and(AppointmentSpecification.toDate(endDate));
        }

        return appointmentRepository.findAll(spec).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getPatient().getId(),
                a.getDoctor() != null ? a.getDoctor().getId() : null,
                a.getDate(),
                a.getSpecialty(),
                a.getProfessional(),
                a.getClinic(),
                a.getSummary(),
                a.getPrescription(),
                a.getMedicalObservation()
        );
    }
}
