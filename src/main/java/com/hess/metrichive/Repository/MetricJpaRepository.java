package com.hess.metrichive.Repository;

import com.hess.metrichive.Model.Metric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface MetricJpaRepository extends JpaRepository<Metric, Long>, JpaSpecificationExecutor<Metric> {

    Optional<Metric> findByIdAndTenantId(Long id, Long tenantId);

    Page<Metric> findByTenantId(Long tenantId, Pageable pageable);

    Page<Metric> findByTenantIdAndMetricName(Long tenantId, String metricName, Pageable pageable);

    Page<Metric> findByTenantIdAndTimestampBetween(Long tenantId, Instant start, Instant end, Pageable pageable);

    Page<Metric> findByTenantIdAndMetricNameAndTimestampBetween(
            Long tenantId, String metricName, Instant start, Instant end, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Metric m WHERE m.tenantId = :tenantId AND m.id = :id")
    int deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Modifying
    @Query("DELETE FROM Metric m WHERE m.tenantId = :tenantId AND m.metricName = :metricName")
    int deleteByTenantIdAndMetricName(@Param("tenantId") Long tenantId, @Param("metricName") String metricName);

    @Modifying
    @Query("DELETE FROM Metric m WHERE m.tenantId = :tenantId AND m.timestamp BETWEEN :start AND :end")
    int deleteByTenantIdAndTimestampBetween(
            @Param("tenantId") Long tenantId, @Param("start") Instant start, @Param("end") Instant end);

    @Modifying
    @Query("DELETE FROM Metric m WHERE m.tenantId = :tenantId AND m.metricName = :metricName AND m.timestamp BETWEEN :start AND :end")
    int deleteByTenantIdAndMetricNameAndTimestampBetween(
            @Param("tenantId") Long tenantId, @Param("metricName") String metricName, @Param("start") Instant start, @Param("end") Instant end);

    @Modifying
    @Query("DELETE FROM Metric m WHERE m.tenantId = :tenantId")
    int deleteByTenantId(@Param("tenantId") Long tenantId);

    long countByTenantId(Long tenantId);
}
