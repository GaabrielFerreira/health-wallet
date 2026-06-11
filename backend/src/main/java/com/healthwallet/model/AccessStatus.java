package com.healthwallet.model;

/**
 * Estado de uma permissão de acesso compartilhado.
 * Calculado a partir de revoked + expiresAt (ver AccessExpirationPolicy).
 */
public enum AccessStatus {
    ACTIVE,
    EXPIRED,
    REVOKED
}
