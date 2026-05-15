package com.healthwallet.repository;

import com.healthwallet.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findAllByPatientIdOrderByGeneratedAtDesc(UUID patientId);
}
