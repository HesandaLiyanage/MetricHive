package com.hess.metrichive.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hess.metrichive.Model.Metric;
import com.hess.metrichive.dto.IntervalData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MetricRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private volatile Boolean isPostgres;

    private boolean isPostgreSQL() {
        if (isPostgres == null) {
            synchronized (this) {
                if (isPostgres == null) {
                    try {
                        String productName = jdbcTemplate.execute((ConnectionCallback<String>) conn ->
                                conn.getMetaData().getDatabaseProductName());
                        isPostgres = productName != null && productName.toLowerCase().contains("postgres");
                    } catch (Exception e) {
                        isPostgres = true;
                    }
                }
            }
        }
        return isPostgres;
    }

    public void batchInsert(List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return;
        }

        String tagsPlaceholder = isPostgreSQL() ? "?::jsonb" : "?";
        String sql = "INSERT INTO metrics_raw (tenant_id, metric_name, \"value\", timestamp, tags) " +
                "VALUES (?, ?, ?, ?, " + tagsPlaceholder + ")";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Metric metric = metrics.get(i);
                ps.setLong(1, metric.getTenantId());
                ps.setString(2, metric.getMetricName());
                ps.setDouble(3, metric.getValue());
                ps.setTimestamp(4, Timestamp.from(metric.getTimestamp()));

                try {
                    String tagsJson = metric.getTags() != null ? objectMapper.writeValueAsString(metric.getTags()) : "{}";
                    ps.setString(5, tagsJson);
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize tags to JSON", e);
                    ps.setString(5, "{}");
                }
            }

            @Override
            public int getBatchSize() {
                return metrics.size();
            }
        });
    }

    public List<IntervalData> executeIntervalQuery(
            Long tenantId,
            String metricName,
            String aggregation,
            Instant startTime,
            Instant endTime,
            String interval,
            Map<String, String> filters,
            String orderBy,
            String order,
            Integer limit) {

        String aggFunc = getSqlAggregationFunction(aggregation);
        String bucketExpr = getBucketExpression(interval);

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT ")
           .append(bucketExpr).append(" AS bucket_time, ")
           .append(aggFunc).append(" AS agg_val, ")
           .append("COUNT(*) AS bucket_count ")
           .append("FROM metrics_raw ")
           .append("WHERE tenant_id = ? ")
           .append("AND metric_name = ? ")
           .append("AND timestamp >= ? ")
           .append("AND timestamp <= ? ");

        params.add(tenantId);
        params.add(metricName);
        params.add(Timestamp.from(startTime));
        params.add(Timestamp.from(endTime));

        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                if (isPostgreSQL()) {
                    sql.append("AND tags ->> ? = ? ");
                    params.add(entry.getKey());
                    params.add(entry.getValue());
                } else {
                    sql.append("AND tags IS NOT NULL ");
                }
            }
        }

        sql.append("GROUP BY bucket_time ");

        String orderDirection = (order != null && order.equalsIgnoreCase("asc")) ? "ASC" : "DESC";
        if ("value".equalsIgnoreCase(orderBy)) {
            sql.append("ORDER BY agg_val ").append(orderDirection);
        } else if ("count".equalsIgnoreCase(orderBy)) {
            sql.append("ORDER BY bucket_count ").append(orderDirection);
        } else {
            sql.append("ORDER BY bucket_time ").append(orderDirection);
        }

        int maxLimit = (limit != null && limit > 0) ? Math.min(limit, 1000) : 100;
        sql.append(" LIMIT ?");
        params.add(maxLimit);

        return jdbcTemplate.query(sql.toString(), (ResultSet rs, int rowNum) -> {
            Timestamp ts = rs.getTimestamp("bucket_time");
            double val = rs.getDouble("agg_val");
            long count = rs.getLong("bucket_count");

            return IntervalData.builder()
                    .timestamp(ts != null ? ts.toInstant() : null)
                    .value(val)
                    .count(count)
                    .build();
        }, params.toArray());
    }

    private String getSqlAggregationFunction(String agg) {
        if (agg == null) return "AVG(\"value\")";
        return switch (agg.toLowerCase()) {
            case "sum" -> "SUM(\"value\")";
            case "min" -> "MIN(\"value\")";
            case "max" -> "MAX(\"value\")";
            case "count" -> "COUNT(\"value\")";
            default -> "AVG(\"value\")";
        };
    }

    private String getBucketExpression(String interval) {
        boolean isPg = isPostgreSQL();
        if (interval == null) return "date_trunc('minute', timestamp)";
        return switch (interval.toLowerCase()) {
            case "5m" -> isPg
                    ? "to_timestamp(floor(extract(epoch from timestamp) / 300) * 300)"
                    : "date_trunc('minute', timestamp)";
            case "1h" -> "date_trunc('hour', timestamp)";
            case "1d" -> "date_trunc('day', timestamp)";
            case "1w" -> "date_trunc('week', timestamp)";
            default -> "date_trunc('minute', timestamp)";
        };
    }
}