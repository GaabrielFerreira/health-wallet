package com.healthwallet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, length = 100)
    private String specialty;

    @Column(nullable = false, length = 100)
    private String professional;

    @Column(length = 100)
    private String clinic;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "medical_observation", columnDefinition = "TEXT")
    private String medicalObservation;
}
