package com.healthwallet.service;

import com.healthwallet.dto.AnamnesisRequest;
import com.healthwallet.dto.AnamnesisResponse;
import com.healthwallet.dto.AnamnesisUpdateRequest;
import com.healthwallet.exception.AnamnesisAlreadyExistsException;
import com.healthwallet.exception.AnamnesisNotFoundException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.model.Anamnesis;
import com.healthwallet.model.User;
import com.healthwallet.repository.AnamnesisRepository;
import com.healthwallet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnamnesisServiceTest {

    @Mock
    private AnamnesisRepository anamnesisRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AnamnesisService anamnesisService;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Test
    void create_savesAnamnesis_withPatientReference() {
        UUID patientId = UUID.randomUUID();
        User patient = buildUser(patientId);
        AnamnesisRequest request = buildRequest(patientId);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(anamnesisRepository.existsByPatientId(patientId)).thenReturn(false);
        when(anamnesisRepository.save(any())).thenAnswer(inv -> {
            Anamnesis a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AnamnesisResponse response = anamnesisService.create(request);

        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getAllergies()).isEqualTo("Dipirona");
        assertThat(response.getBloodType()).isEqualTo("O+");

        ArgumentCaptor<Anamnesis> captor = ArgumentCaptor.forClass(Anamnesis.class);
        verify(anamnesisRepository).save(captor.capture());
        assertThat(captor.getValue().getPatient().getId()).isEqualTo(patientId);
    }

    @Test
    void create_throwsPatientNotFoundException_whenPatientNotFound() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> anamnesisService.create(buildRequest(patientId)))
                .isInstanceOf(PatientNotFoundException.class);

        verify(anamnesisRepository, never()).save(any());
    }

    @Test
    void create_throwsAnamnesisAlreadyExists_whenPatientAlreadyHasAnamnesis() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.of(buildUser(patientId)));
        when(anamnesisRepository.existsByPatientId(patientId)).thenReturn(true);

        assertThatThrownBy(() -> anamnesisService.create(buildRequest(patientId)))
                .isInstanceOf(AnamnesisAlreadyExistsException.class);

        verify(anamnesisRepository, never()).save(any());
    }

    // ── GET BY PATIENT ID ─────────────────────────────────────────────────────

    @Test
    void getByPatientId_returnsResponse_whenFound() {
        UUID patientId = UUID.randomUUID();
        User patient = buildUser(patientId);
        Anamnesis anamnesis = buildAnamnesis(patient);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(anamnesisRepository.findByPatientId(patientId)).thenReturn(Optional.of(anamnesis));

        AnamnesisResponse response = anamnesisService.getByPatientId(patientId);

        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getAllergies()).isEqualTo("Dipirona");
    }

    @Test
    void getByPatientId_throwsPatientNotFoundException_whenPatientMissing() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> anamnesisService.getByPatientId(patientId))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void getByPatientId_throwsAnamnesisNotFoundException_whenAnamnesisNotFound() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.of(buildUser(patientId)));
        when(anamnesisRepository.findByPatientId(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> anamnesisService.getByPatientId(patientId))
                .isInstanceOf(AnamnesisNotFoundException.class);
    }

    // ── LIST BY PATIENT ───────────────────────────────────────────────────────

    @Test
    void listByPatientId_returnsListLinkedToPatient() {
        UUID patientId = UUID.randomUUID();
        User patient = buildUser(patientId);
        Anamnesis anamnesis = buildAnamnesis(patient);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(anamnesisRepository.findAllByPatientId(patientId)).thenReturn(List.of(anamnesis));

        List<AnamnesisResponse> result = anamnesisService.listByPatientId(patientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(patientId);
    }

    @Test
    void listByPatientId_returnsEmptyList_whenNoAnamnesis() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.of(buildUser(patientId)));
        when(anamnesisRepository.findAllByPatientId(patientId)).thenReturn(List.of());

        List<AnamnesisResponse> result = anamnesisService.listByPatientId(patientId);

        assertThat(result).isEmpty();
    }

    @Test
    void listByPatientId_throwsPatientNotFoundException_whenPatientNotFound() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> anamnesisService.listByPatientId(patientId))
                .isInstanceOf(PatientNotFoundException.class);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Test
    void update_updatesFields_andReturnsUpdatedResponse() {
        UUID patientId = UUID.randomUUID();
        User patient = buildUser(patientId);
        Anamnesis anamnesis = buildAnamnesis(patient);
        AnamnesisUpdateRequest updateRequest = buildUpdateRequest();

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(anamnesisRepository.findByPatientId(patientId)).thenReturn(Optional.of(anamnesis));
        when(anamnesisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnamnesisResponse response = anamnesisService.update(patientId, updateRequest);

        assertThat(response.getAllergies()).isEqualTo("Amoxicilina");
        assertThat(response.getChronicDiseases()).isEqualTo("Diabetes tipo 2");
    }

    @Test
    void update_throwsPatientNotFoundException_whenPatientNotFound() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> anamnesisService.update(patientId, buildUpdateRequest()))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void update_throwsAnamnesisNotFoundException_whenAnamnesisNotFound() {
        UUID patientId = UUID.randomUUID();
        when(userRepository.findById(patientId)).thenReturn(Optional.of(buildUser(patientId)));
        when(anamnesisRepository.findByPatientId(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> anamnesisService.update(patientId, buildUpdateRequest()))
                .isInstanceOf(AnamnesisNotFoundException.class);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private User buildUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName("João Silva");
        user.setEmail("joao@email.com");
        return user;
    }

    private Anamnesis buildAnamnesis(User patient) {
        Anamnesis a = new Anamnesis();
        a.setId(UUID.randomUUID());
        a.setPatient(patient);
        a.setAllergies("Dipirona");
        a.setChronicDiseases("Hipertensão");
        a.setMedications("Losartana 50mg");
        a.setBloodType("O+");
        a.setFamilyHistory("Pai diabético");
        a.setObservations("Nenhuma");
        return a;
    }

    private AnamnesisRequest buildRequest(UUID patientId) {
        AnamnesisRequest r = new AnamnesisRequest();
        r.setPatientId(patientId);
        r.setAllergies("Dipirona");
        r.setChronicDiseases("Hipertensão");
        r.setMedications("Losartana 50mg");
        r.setBloodType("O+");
        r.setFamilyHistory("Pai diabético");
        r.setObservations("Nenhuma");
        return r;
    }

    private AnamnesisUpdateRequest buildUpdateRequest() {
        AnamnesisUpdateRequest r = new AnamnesisUpdateRequest();
        r.setAllergies("Amoxicilina");
        r.setChronicDiseases("Diabetes tipo 2");
        r.setMedications("Metformina");
        r.setBloodType("A+");
        r.setFamilyHistory("Mãe diabética");
        r.setObservations("Revisão anual");
        return r;
    }
}
