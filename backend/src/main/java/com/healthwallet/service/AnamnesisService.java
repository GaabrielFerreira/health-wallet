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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnamnesisService {

    private final AnamnesisRepository anamnesisRepository;
    private final UserRepository userRepository;

    public AnamnesisResponse create(AnamnesisRequest request) {
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(request.getPatientId()));

        if (anamnesisRepository.existsByPatientId(request.getPatientId())) {
            throw new AnamnesisAlreadyExistsException();
        }

        Anamnesis anamnesis = new Anamnesis();
        anamnesis.setPatient(patient);
        anamnesis.setAllergies(request.getAllergies());
        anamnesis.setChronicDiseases(request.getChronicDiseases());
        anamnesis.setMedications(request.getMedications());
        anamnesis.setBloodType(request.getBloodType());
        anamnesis.setFamilyHistory(request.getFamilyHistory());
        anamnesis.setObservations(request.getObservations());

        Anamnesis saved = anamnesisRepository.save(anamnesis);

        return new AnamnesisResponse(
                saved.getId(),
                saved.getPatient().getId(),
                saved.getAllergies(),
                saved.getChronicDiseases(),
                saved.getMedications(),
                saved.getBloodType(),
                saved.getFamilyHistory(),
                saved.getObservations()
        );
    }

    public AnamnesisResponse getByPatientId(UUID patientId) {
        userRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        Anamnesis anamnesis = anamnesisRepository.findByPatientId(patientId)
                .orElseThrow(() -> new AnamnesisNotFoundException(patientId));

        return new AnamnesisResponse(
                anamnesis.getId(),
                anamnesis.getPatient().getId(),
                anamnesis.getAllergies(),
                anamnesis.getChronicDiseases(),
                anamnesis.getMedications(),
                anamnesis.getBloodType(),
                anamnesis.getFamilyHistory(),
                anamnesis.getObservations()
        );
    }

    public List<AnamnesisResponse> listByPatientId(UUID patientId) {
        userRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        return anamnesisRepository.findAllByPatientId(patientId).stream()
                .map(a -> new AnamnesisResponse(
                        a.getId(),
                        a.getPatient().getId(),
                        a.getAllergies(),
                        a.getChronicDiseases(),
                        a.getMedications(),
                        a.getBloodType(),
                        a.getFamilyHistory(),
                        a.getObservations()
                ))
                .collect(Collectors.toList());
    }

    public AnamnesisResponse update(UUID patientId, AnamnesisUpdateRequest request) {
        userRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        Anamnesis anamnesis = anamnesisRepository.findByPatientId(patientId)
                .orElseThrow(() -> new AnamnesisNotFoundException(patientId));

        anamnesis.setAllergies(request.getAllergies());
        anamnesis.setChronicDiseases(request.getChronicDiseases());
        anamnesis.setMedications(request.getMedications());
        anamnesis.setBloodType(request.getBloodType());
        anamnesis.setFamilyHistory(request.getFamilyHistory());
        anamnesis.setObservations(request.getObservations());

        Anamnesis saved = anamnesisRepository.save(anamnesis);

        return new AnamnesisResponse(
                saved.getId(),
                saved.getPatient().getId(),
                saved.getAllergies(),
                saved.getChronicDiseases(),
                saved.getMedications(),
                saved.getBloodType(),
                saved.getFamilyHistory(),
                saved.getObservations()
        );
    }
}
