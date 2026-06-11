package com.healthwallet.service.access;

import com.healthwallet.model.AccessStatus;
import com.healthwallet.model.SharedReport;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Componente com responsabilidade única (SRP) de determinar o estado de expiração
 * de uma permissão de acesso. Centraliza a regra que antes estava espalhada/inline,
 * permitindo que validators e services dependam desta abstração (DIP).
 */
@Component
public class AccessExpirationPolicy {

    public boolean isExpired(SharedReport access) {
        return access.getExpiresAt() != null
                && access.getExpiresAt().isBefore(LocalDateTime.now());
    }

    public boolean isActive(SharedReport access) {
        return !Boolean.TRUE.equals(access.getRevoked()) && !isExpired(access);
    }

    public AccessStatus statusOf(SharedReport access) {
        if (Boolean.TRUE.equals(access.getRevoked())) {
            return AccessStatus.REVOKED;
        }
        if (isExpired(access)) {
            return AccessStatus.EXPIRED;
        }
        return AccessStatus.ACTIVE;
    }
}
