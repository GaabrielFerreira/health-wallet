package com.healthwallet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "vaccines")
public class Vaccine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 50)
    private String lot;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Dose dose;

    @Column(length = 500)
    private String proof;

    @Column(columnDefinition = "TEXT")
    private String observations;
}
