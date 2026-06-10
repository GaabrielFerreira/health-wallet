package com.healthwallet.repository;

import com.healthwallet.model.SharedAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SharedAccessLogRepository extends JpaRepository<SharedAccessLog, UUID> {
}
