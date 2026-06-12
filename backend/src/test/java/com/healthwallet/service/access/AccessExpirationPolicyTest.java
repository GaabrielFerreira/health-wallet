package com.healthwallet.service.access;

import com.healthwallet.model.AccessStatus;
import com.healthwallet.model.SharedReport;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccessExpirationPolicyTest {

    private final AccessExpirationPolicy policy = new AccessExpirationPolicy();

    @Test
    void isExpired_returnsFalse_whenExpiresAtIsNull() {
        SharedReport access = new SharedReport();
        access.setExpiresAt(null);

        assertThat(policy.isExpired(access)).isFalse();
    }

    @Test
    void isExpired_returnsFalse_whenExpiresAtIsInFuture() {
        SharedReport access = new SharedReport();
        access.setExpiresAt(LocalDateTime.now().plusDays(1));

        assertThat(policy.isExpired(access)).isFalse();
    }

    @Test
    void isExpired_returnsTrue_whenExpiresAtIsInPast() {
        SharedReport access = new SharedReport();
        access.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThat(policy.isExpired(access)).isTrue();
    }

    @Test
    void isActive_returnsTrue_whenNotRevokedAndNotExpired() {
        SharedReport access = new SharedReport();
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().plusDays(1));

        assertThat(policy.isActive(access)).isTrue();
    }

    @Test
    void isActive_returnsFalse_whenRevoked() {
        SharedReport access = new SharedReport();
        access.setRevoked(true);
        access.setExpiresAt(LocalDateTime.now().plusDays(1));

        assertThat(policy.isActive(access)).isFalse();
    }

    @Test
    void isActive_returnsFalse_whenExpired() {
        SharedReport access = new SharedReport();
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThat(policy.isActive(access)).isFalse();
    }

    @Test
    void statusOf_returnsRevoked_whenRevoked() {
        SharedReport access = new SharedReport();
        access.setRevoked(true);
        access.setExpiresAt(LocalDateTime.now().minusDays(1)); // revogado tem prioridade

        assertThat(policy.statusOf(access)).isEqualTo(AccessStatus.REVOKED);
    }

    @Test
    void statusOf_returnsExpired_whenNotRevokedButExpired() {
        SharedReport access = new SharedReport();
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThat(policy.statusOf(access)).isEqualTo(AccessStatus.EXPIRED);
    }

    @Test
    void statusOf_returnsActive_whenNotRevokedAndNotExpired() {
        SharedReport access = new SharedReport();
        access.setRevoked(false);
        access.setExpiresAt(LocalDateTime.now().plusDays(1));

        assertThat(policy.statusOf(access)).isEqualTo(AccessStatus.ACTIVE);
    }

    @Test
    void statusOf_returnsActive_whenExpiresAtIsNull() {
        SharedReport access = new SharedReport();
        access.setRevoked(false);
        access.setExpiresAt(null);

        assertThat(policy.statusOf(access)).isEqualTo(AccessStatus.ACTIVE);
    }
}
