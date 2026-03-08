-- The IF NOT EXISTS ensures it doesn't crash if it runs twice
CREATE TABLE IF NOT EXISTS metrics_raw (
                                           id BIGSERIAL PRIMARY KEY,
                                           tenant_id BIGINT NOT NULL,
                                           metric_name VARCHAR(255) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    tags JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_metrics_tenant_name_time
    ON metrics_raw(tenant_id, metric_name, timestamp DESC);