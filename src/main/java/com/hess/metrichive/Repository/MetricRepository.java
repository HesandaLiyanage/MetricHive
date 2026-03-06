package com.hess.metrichive.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hess.metrichive.Model.Metric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MetricRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper; // For converting Map<String, String> to JSONB (if using PostgreSQL)

    public void batchInsert(List<Metric> metrics) {
        String sql = "INSERT INTO metrics_raw (tenant_id, metric_name, value, timestamp, tags) " +
                "VALUES (?, ?, ?, ?, ?::jsonb)"; // Assuming PostgreSQL with JSONB column for tags

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Metric metric = metrics.get(i);
                ps.setLong(1, metric.getTenantId());
                ps.setString(2, metric.getMetricName());
                ps.setDouble(3, metric.getValue());
                ps.setTimestamp(4, Timestamp.from(Instant.from(metric.getTimestamp())));

                // Handle tags mapping to JSON
                try {
                    String tagsJson = metric.getTags() != null ? objectMapper.writeValueAsString(metric.getTags()) : "{}";
                    ps.setString(5, tagsJson);
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize tags to JSON", e);
                    ps.setString(5, "{}"); // Fallback
                }
            }


            //this shows how many records have been saved
            @Override
            public int getBatchSize() {
                return metrics.size();
            }
        });
    }
}