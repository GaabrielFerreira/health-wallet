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
@Table(name = "reports")
public class Report extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ReportType type;

    @Column(name = "content_url", length = 500)
    private String contentUrl;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PROCESSING;

    @Column(name = "filter_params", columnDefinition = "TEXT")
    private String filterParams;

    @Column(columnDefinition = "TEXT")
    private String observations;
}
