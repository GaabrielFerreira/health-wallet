package com.healthwallet.listener;

import com.healthwallet.event.AnamnesisUpdatedEvent;
import com.healthwallet.model.Anamnesis;
import com.healthwallet.model.AnamnesisHistory;
import com.healthwallet.repository.AnamnesisHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer concreto que escuta AnamnesisUpdatedEvent e persiste o estado anterior
 * da anamnese em anamnesis_history. Mantém o AnamnesisService livre desse detalhe.
 */
@Component
@RequiredArgsConstructor
public class AnamnesisHistoryListener {

    private final AnamnesisHistoryRepository historyRepository;

    @EventListener
    public void onAnamnesisUpdated(AnamnesisUpdatedEvent event) {
        Anamnesis snapshot = event.getPreviousState();

        AnamnesisHistory history = new AnamnesisHistory();
        history.setAnamnesis(snapshot);
        history.setAllergies(snapshot.getAllergies());
        history.setChronicDiseases(snapshot.getChronicDiseases());
        history.setMedications(snapshot.getMedications());
        history.setBloodType(snapshot.getBloodType());
        history.setChangedBy(event.getChangedBy());

        historyRepository.save(history);
    }
}
