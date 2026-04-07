package com.healthwallet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "shared_access_logs")
public class SharedAccessLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_report_id", nullable = false)
    private SharedReport sharedReport;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "accessed_data", columnDefinition = "TEXT")
    private String accessedData;
}
