package com.healthwallet.repository;

import com.healthwallet.model.Anamnesis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnamnesisRepository extends JpaRepository<Anamnesis, UUID> {

    boolean existsByPatientId(UUID patientId);

    Optional<Anamnesis> findByPatientId(UUID patientId);

    List<Anamnesis> findAllByPatientId(UUID patientId);
}
