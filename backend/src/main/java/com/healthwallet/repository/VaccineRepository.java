package com.healthwallet.repository;

import com.healthwallet.model.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VaccineRepository extends JpaRepository<Vaccine, UUID> {
    List<Vaccine> findByPatientId(UUID patientId);
}
