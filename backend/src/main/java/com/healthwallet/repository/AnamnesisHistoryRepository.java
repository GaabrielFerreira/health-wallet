package com.healthwallet.repository;

import com.healthwallet.model.AnamnesisHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnamnesisHistoryRepository extends JpaRepository<AnamnesisHistory, UUID> {

    List<AnamnesisHistory> findAllByAnamnesisIdOrderByCreatedAtDesc(UUID anamnesisId);
}
