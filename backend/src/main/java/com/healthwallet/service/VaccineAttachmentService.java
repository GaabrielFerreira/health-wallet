package com.healthwallet.service;

import com.healthwallet.dto.VaccineAttachmentResponse;
import com.healthwallet.exception.InvalidAttachmentException;
import com.healthwallet.exception.VaccineAttachmentNotFoundException;
import com.healthwallet.exception.VaccineNotFoundException;
import com.healthwallet.model.AttachmentType;
import com.healthwallet.model.Vaccine;
import com.healthwallet.model.VaccineAttachment;
import com.healthwallet.repository.VaccineAttachmentRepository;
import com.healthwallet.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Responsabilidade única: gerenciar anexos (comprovantes) de vacinas.
 * Extraído de VaccineService para satisfazer SRP.
 */
@Service
@RequiredArgsConstructor
public class VaccineAttachmentService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Map<String, AttachmentType> ALLOWED_TYPES = Map.of(
            "application/pdf", AttachmentType.PDF,
            "image/jpeg", AttachmentType.JPG,
            "image/png", AttachmentType.PNG
    );

    private final VaccineRepository vaccineRepository;
    private final VaccineAttachmentRepository vaccineAttachmentRepository;

    @Transactional
    public VaccineAttachmentResponse upload(UUID vaccineId, MultipartFile file) {
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
    public VaccineAttachment get(UUID vaccineId) {
        vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new VaccineNotFoundException(vaccineId));

        return vaccineAttachmentRepository.findByVaccineId(vaccineId)
                .orElseThrow(() -> new VaccineAttachmentNotFoundException(vaccineId));
    }
}
