package com.healthwallet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "anamnesis_attachments")
public class AnamnesisAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnesis_id", nullable = false)
    private Anamnesis anamnesis;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private AttachmentType type;
}
