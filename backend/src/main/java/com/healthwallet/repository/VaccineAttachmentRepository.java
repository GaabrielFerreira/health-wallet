package com.healthwallet.repository;

import com.healthwallet.model.VaccineAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VaccineAttachmentRepository extends JpaRepository<VaccineAttachment, UUID> {

    Optional<VaccineAttachment> findByVaccineId(UUID vaccineId);

    boolean existsByVaccineId(UUID vaccineId);

    @Query("select va.vaccine.id from VaccineAttachment va where va.vaccine.id in :vaccineIds")
    List<UUID> findVaccineIdsWithAttachment(List<UUID> vaccineIds);
}
