package com.healthwallet.repository;

import com.healthwallet.model.Report;
import com.healthwallet.model.ReportStatus;
import com.healthwallet.model.ReportType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class ReportSpecification {

    private ReportSpecification() {}

    public static Specification<Report> byPatientId(UUID patientId) {
        return (root, query, cb) -> cb.equal(root.get("patient").get("id"), patientId);
    }

    public static Specification<Report> byType(ReportType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Report> byStatus(ReportStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Report> fromDate(LocalDate startDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("generatedAt"), startDate.atStartOfDay());
    }

    public static Specification<Report> toDate(LocalDate endDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("generatedAt"), endDate.atTime(23, 59, 59));
    }
}
