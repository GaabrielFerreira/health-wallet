package com.healthwallet.service;

import com.healthwallet.dto.AnamnesisRequest;
import com.healthwallet.dto.AnamnesisResponse;
import com.healthwallet.exception.AnamnesisAlreadyExistsException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.model.Anamnesis;
import com.healthwallet.model.User;
import com.healthwallet.repository.AnamnesisRepository;
import com.healthwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
