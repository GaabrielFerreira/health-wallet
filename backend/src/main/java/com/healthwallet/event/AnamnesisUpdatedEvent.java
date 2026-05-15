package com.healthwallet.event;

import com.healthwallet.model.Anamnesis;
import com.healthwallet.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Evento publicado quando uma anamnese é atualizada.
 * Observers (listeners) reagem para registrar o histórico, notificar usuários, etc.
 * Aplica o padrão Observer via ApplicationEventPublisher do Spring.
 */
@Getter
@RequiredArgsConstructor
public class AnamnesisUpdatedEvent {

    private final Anamnesis previousState;
    private final User changedBy;
}
