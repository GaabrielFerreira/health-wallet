package com.healthwallet.repository;

import com.healthwallet.model.Appointment;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class AppointmentSpecification {

    private AppointmentSpecification() {}

    public static Specification<Appointment> byPatientId(UUID patientId) {
        return (root, query, cb) -> cb.equal(root.get("patient").get("id"), patientId);
    }

    public static Specification<Appointment> bySpecialty(String specialty) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("specialty")), "%" + specialty.toLowerCase() + "%");
    }

    public static Specification<Appointment> byProfessional(String professional) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("professional")), "%" + professional.toLowerCase() + "%");
    }

    public static Specification<Appointment> fromDate(LocalDate startDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate.atStartOfDay());
    }

    public static Specification<Appointment> toDate(LocalDate endDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate.atTime(23, 59, 59));
    }
}
