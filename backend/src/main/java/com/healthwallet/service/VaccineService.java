package com.healthwallet.service;

import com.healthwallet.dto.VaccineAttachmentResponse;
import com.healthwallet.dto.VaccineRequest;
import com.healthwallet.dto.VaccineResponse;
import com.healthwallet.exception.InvalidAttachmentException;
import com.healthwallet.exception.PatientNotFoundException;
import com.healthwallet.exception.VaccineAttachmentNotFoundException;
import com.healthwallet.exception.VaccineNotFoundException;
import com.healthwallet.model.AttachmentType;
import com.healthwallet.model.Vaccine;
import com.healthwallet.model.VaccineAttachment;
import com.healthwallet.repository.UserRepository;
import com.healthwallet.repository.VaccineAttachmentRepository;
import com.healthwallet.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccineService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Map<String, AttachmentType> ALLOWED_TYPES = Map.of(
            "application/pdf", AttachmentType.PDF,
            "image/jpeg", AttachmentType.JPG,
            "image/png", AttachmentType.PNG
    );

    private final VaccineRepository vaccineRepository;
    private final VaccineAttachmentRepository vaccineAttachmentRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public VaccineAttachmentResponse uploadProof(UUID vaccineId, MultipartFile file) {
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new VaccineNotFoundException(vaccineId));

        if (file == null || file.isEmpty()) {
            throw new InvalidAttachmentException("Arquivo do comprovante é obrigatório");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidAttachmentException("Arquivo excede o tamanho máximo de 5MB");
        }

        AttachmentType type = ALLOWED_TYPES.get(file.getContentType());
        if (type == null) {
            throw new InvalidAttachmentException("Tipo de arquivo não suportado. Use PDF, JPG ou PNG");
        }

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new InvalidAttachmentException("Não foi possível ler o arquivo enviado");
        }

        VaccineAttachment attachment = vaccineAttachmentRepository.findByVaccineId(vaccineId)
                .orElseGet(VaccineAttachment::new);
        attachment.setVaccine(vaccine);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setType(type);
        attachment.setData(data);

        VaccineAttachment saved = vaccineAttachmentRepository.save(attachment);

        return new VaccineAttachmentResponse(
                saved.getId(),
                vaccineId,
                saved.getFileName(),
                saved.getContentType(),
                saved.getFileSize(),
                saved.getType()
        );
    }

    @Transactional(readOnly = true)
    public VaccineAttachment getProof(UUID vaccineId) {
        vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new VaccineNotFoundException(vaccineId));

        return vaccineAttachmentRepository.findByVaccineId(vaccineId)
                .orElseThrow(() -> new VaccineAttachmentNotFoundException(vaccineId));
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
