package com.healthwallet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "anamnesis")
public class Anamnesis extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "chronic_diseases", columnDefinition = "TEXT")
    private String chronicDiseases;

    @Column(columnDefinition = "TEXT")
    private String medications;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    @Column(name = "family_history", columnDefinition = "TEXT")
    private String familyHistory;

    @Column(columnDefinition = "TEXT")
    private String observations;
}
