package com.hess.metrichive.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "metrics_raw")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "\"value\"", nullable = false)
    private Double value;

    @Column(nullable = false)
    private Instant timestamp;

    @JdbcTypeCode(SqlTypes.JSON) //"Take this Java object, convert it into a JSON string, and save it in a JSON column in the database."
    @Column(columnDefinition = "jsonb")
    private Map<String, String> tags;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

//    @Column(name="updated_at")
//    private LocalDateTime updatedAt;

    @PrePersist //only runs when the object is created. so one time.
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

//    @PreUpdate
//    protected  void OnUpdate() { updatedAt = LocalDateTime.now(); }
}