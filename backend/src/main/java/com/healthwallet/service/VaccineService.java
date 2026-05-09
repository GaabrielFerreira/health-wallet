package com.healthwallet.service;

import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.model.Vaccine;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccineService {

    private final VaccineRepository vaccineRepository;
    private final UserRepository userRepository;

    public List<VaccineResponse> listByPatientId(UUID patientId) {
        userRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        return vaccineRepository.findByPatientId(patientId).stream()
                .map(v -> new VaccineResponse(
                        v.getId(),
                        v.getPatient().getId(),
                        v.getName(),
                        v.getManufacturer(),
                        v.getLot(),
                        v.getApplicationDate(),
                        v.getDose(),
                        v.getProof(),
                        v.getObservations()
                ))
                .collect(Collectors.toList());
    }

    public VaccineResponse create(VaccineRequest request) {
        var patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(request.getPatientId()));

        var vaccine = new Vaccine();
        vaccine.setPatient(patient);
        vaccine.setName(request.getName());
        vaccine.setManufacturer(request.getManufacturer());
        vaccine.setLot(request.getLot());
        vaccine.setApplicationDate(request.getApplicationDate());
        vaccine.setDose(request.getDose());
        vaccine.setProof(request.getProof());
        vaccine.setObservations(request.getObservations());

        var saved = vaccineRepository.save(vaccine);

        return new VaccineResponse(
                saved.getId(),
                saved.getPatient().getId(),
                saved.getName(),
                saved.getManufacturer(),
                saved.getLot(),
                saved.getApplicationDate(),
                saved.getDose(),
                saved.getProof(),
                saved.getObservations()
        );
    }
}
