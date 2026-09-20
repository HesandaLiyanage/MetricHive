package com.hess.metrichive.Service;

import com.hess.metrichive.Exception.ResourceNotFoundException;
import com.hess.metrichive.Model.Metric;
import com.hess.metrichive.Repository.MetricJpaRepository;
import com.hess.metrichive.Repository.MetricRepository;
import com.hess.metrichive.dto.IntervalData;
import com.hess.metrichive.dto.MetricResponse;
import com.hess.metrichive.dto.PageResponse;
import com.hess.metrichive.dto.QueryMetadata;
import com.hess.metrichive.dto.QueryRequest;
import com.hess.metrichive.dto.QueryResult;
import com.hess.metrichive.dto.QueryResponse;
import com.hess.metrichive.dto.UpdateMetricRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricQueryService {

    private final MetricRepository metricRepository;
    private final MetricJpaRepository metricJpaRepository;

    @Transactional(readOnly = true)
    public QueryResponse query(Long tenantId, QueryRequest request) {
        long startTimeMs = System.currentTimeMillis();

        List<IntervalData> intervals = metricRepository.executeIntervalQuery(
                tenantId,
                request.getMetricName(),
                request.getAggregation(),
                request.getStartTime(),
                request.getEndTime(),
                request.getInterval(),
                request.getFilters(),
                request.getOrderBy(),
                request.getOrder(),
                request.getLimit()
        );

        double total = 0.0;
        long totalCount = 0;
        for (IntervalData interval : intervals) {
            total += (interval.getValue() != null ? interval.getValue() : 0.0);
            totalCount += (interval.getCount() != null ? interval.getCount() : 0);
        }
        double average = intervals.isEmpty() ? 0.0 : (total / intervals.size());

        QueryResult queryResult = QueryResult.builder()
                .dimensions(request.getFilters())
                .intervals(intervals)
                .total(total)
                .average(average)
                .count(totalCount)
                .build();

        List<QueryResult> results = new ArrayList<>();
        results.add(queryResult);

        long queryTimeMs = System.currentTimeMillis() - startTimeMs;
        String requestId = MDC.get("request_id");

        QueryMetadata metadata = QueryMetadata.builder()
                .totalResults(intervals.size())
                .cached(false)
                .cacheHitRate(0.0)
                .queryTimeMs(queryTimeMs)
                .rowsScanned(totalCount)
                .requestId(requestId != null ? requestId : "req_unknown")
                .build();

        return QueryResponse.builder()
                .metricName(request.getMetricName())
                .aggregation(request.getAggregation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .interval(request.getInterval())
                .results(results)
                .metadata(metadata)
                .build();
    }

    @Transactional(readOnly = true)
    public MetricResponse getMetricById(Long tenantId, Long metricId) {
        Metric metric = metricJpaRepository.findByIdAndTenantId(metricId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with id: " + metricId));
        return MetricResponse.from(metric);
    }

    @Transactional(readOnly = true)
    public PageResponse<MetricResponse> listMetrics(
            Long tenantId,
            String metricName,
            Instant startTime,
            Instant endTime,
            Pageable pageable) {

        Page<Metric> page;

        if (metricName != null && !metricName.isBlank() && startTime != null && endTime != null) {
            page = metricJpaRepository.findByTenantIdAndMetricNameAndTimestampBetween(
                    tenantId, metricName.trim(), startTime, endTime, pageable);
        } else if (metricName != null && !metricName.isBlank()) {
            page = metricJpaRepository.findByTenantIdAndMetricName(tenantId, metricName.trim(), pageable);
        } else if (startTime != null && endTime != null) {
            page = metricJpaRepository.findByTenantIdAndTimestampBetween(tenantId, startTime, endTime, pageable);
        } else {
            page = metricJpaRepository.findByTenantId(tenantId, pageable);
        }

        return PageResponse.from(page.map(MetricResponse::from));
    }

    @Transactional
    public MetricResponse updateMetric(Long tenantId, Long metricId, UpdateMetricRequest request) {
        Metric metric = metricJpaRepository.findByIdAndTenantId(metricId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found with id: " + metricId));

        if (request.getValue() != null) {
            metric.setValue(request.getValue());
        }
        if (request.getTimestamp() != null) {
            metric.setTimestamp(request.getTimestamp());
        }
        if (request.getTags() != null) {
            metric.setTags(request.getTags());
        }

        Metric updated = metricJpaRepository.save(metric);
        log.info("Metric {} updated for tenant {}", metricId, tenantId);
        return MetricResponse.from(updated);
    }

    @Transactional
    public void deleteMetric(Long tenantId, Long metricId) {
        int deleted = metricJpaRepository.deleteByIdAndTenantId(metricId, tenantId);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Metric not found with id: " + metricId);
        }
        log.info("Metric {} deleted for tenant {}", metricId, tenantId);
    }

    @Transactional
    public int deleteMetrics(Long tenantId, String metricName, Instant startTime, Instant endTime) {
        int count;
        if (metricName != null && !metricName.isBlank() && startTime != null && endTime != null) {
            count = metricJpaRepository.deleteByTenantIdAndMetricNameAndTimestampBetween(tenantId, metricName.trim(), startTime, endTime);
        } else if (metricName != null && !metricName.isBlank()) {
            count = metricJpaRepository.deleteByTenantIdAndMetricName(tenantId, metricName.trim());
        } else if (startTime != null && endTime != null) {
            count = metricJpaRepository.deleteByTenantIdAndTimestampBetween(tenantId, startTime, endTime);
        } else {
            count = metricJpaRepository.deleteByTenantId(tenantId);
        }
        log.info("Deleted {} metrics for tenant {}", count, tenantId);
        return count;
    }
}
