package com.healthwallet.service;

import com.healthwallet.dto.VaccineAttachmentResponse;
import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.model.Vaccine;
import com.healthwallet.model.VaccineAttachment;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.repository.VaccineAttachmentRepository;
import com.healthwallet.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccineService {

    private final VaccineRepository vaccineRepository;
    private final VaccineAttachmentRepository vaccineAttachmentRepository;
    private final UserRepository userRepository;
    private final VaccineAttachmentService attachmentService;

    public List<VaccineResponse> listByPatientId(UUID patientId) {
        userRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        List<Vaccine> vaccines = vaccineRepository.findByPatientId(patientId);

        Set<UUID> withAttachment = Set.copyOf(vaccineAttachmentRepository.findVaccineIdsWithAttachment(
                vaccines.stream().map(Vaccine::getId).collect(Collectors.toList())));

        return vaccines.stream()
                .map(v -> toResponse(v, withAttachment.contains(v.getId())))
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

        return toResponse(saved, false);
    }

    /** Delega ao VaccineAttachmentService (SRP). Mantido para compatibilidade. */
    public VaccineAttachmentResponse uploadProof(UUID vaccineId, MultipartFile file) {
        return attachmentService.upload(vaccineId, file);
    }

    /** Delega ao VaccineAttachmentService (SRP). Mantido para compatibilidade. */
    public VaccineAttachment getProof(UUID vaccineId) {
        return attachmentService.get(vaccineId);
    }

    private VaccineResponse toResponse(Vaccine v, boolean hasProof) {
        return new VaccineResponse(
                v.getId(),
                v.getPatient().getId(),
                v.getName(),
                v.getManufacturer(),
                v.getLot(),
                v.getApplicationDate(),
                v.getDose(),
                v.getProof(),
                v.getObservations(),
                hasProof
        );
    }
}
