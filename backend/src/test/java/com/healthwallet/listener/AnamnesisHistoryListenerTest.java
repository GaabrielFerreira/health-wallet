package com.healthwallet.listener;

import com.healthwallet.event.AnamnesisUpdatedEvent;
import com.healthwallet.model.Anamnesis;
import com.healthwallet.model.AnamnesisHistory;
import com.healthwallet.model.User;
import com.healthwallet.repository.AnamnesisHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnamnesisHistoryListenerTest {

    @Mock
    private AnamnesisHistoryRepository historyRepository;

    @InjectMocks
    private AnamnesisHistoryListener listener;

    @Test
    void onAnamnesisUpdated_persistsSnapshotAsHistory() {
        User patient = new User();
        patient.setId(UUID.randomUUID());
        patient.setName("João");

        Anamnesis snapshot = new Anamnesis();
        snapshot.setId(UUID.randomUUID());
        snapshot.setPatient(patient);
        snapshot.setAllergies("Dipirona");
        snapshot.setChronicDiseases("Hipertensão");
        snapshot.setMedications("Losartana 50mg");
        snapshot.setBloodType("O+");

        AnamnesisUpdatedEvent event = new AnamnesisUpdatedEvent(snapshot, patient);

        listener.onAnamnesisUpdated(event);

        ArgumentCaptor<AnamnesisHistory> captor = ArgumentCaptor.forClass(AnamnesisHistory.class);
        verify(historyRepository).save(captor.capture());

        AnamnesisHistory saved = captor.getValue();
        assertThat(saved.getAnamnesis()).isSameAs(snapshot);
        assertThat(saved.getAllergies()).isEqualTo("Dipirona");
        assertThat(saved.getChronicDiseases()).isEqualTo("Hipertensão");
        assertThat(saved.getMedications()).isEqualTo("Losartana 50mg");
        assertThat(saved.getBloodType()).isEqualTo("O+");
        assertThat(saved.getChangedBy()).isSameAs(patient);
    }
}
