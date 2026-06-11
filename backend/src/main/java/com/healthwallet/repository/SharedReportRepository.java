package com.healthwallet.repository;

import com.healthwallet.model.SharedReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SharedReportRepository extends JpaRepository<SharedReport, UUID> {

    Optional<SharedReport> findByPatientIdAndDoctorIdAndRevokedFalse(UUID patientId, UUID doctorId);

    List<SharedReport> findByPatientIdAndDoctorIsNotNullOrderByCreatedAtDesc(UUID patientId);

    List<SharedReport> findByDoctorIdAndRevokedFalseOrderByCreatedAtDesc(UUID doctorId);

    boolean existsByPatientIdAndDoctorIdAndRevokedFalse(UUID patientId, UUID doctorId);
}
