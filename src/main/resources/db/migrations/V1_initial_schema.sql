-- Tenants table
CREATE TABLE tenants (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         api_key VARCHAR(255) UNIQUE NOT NULL,
                         tier VARCHAR(50) DEFAULT 'free',
                         max_metrics_per_day INT DEFAULT 10000,
                         max_requests_per_minute INT DEFAULT 60,
                         created_at TIMESTAMPTZ DEFAULT NOW(),
                         updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Insert a test tenant for development
INSERT INTO tenants (name, email, api_key, tier)
VALUES ('Test Tenant', 'test@metrichive.com', 'test_key_12345', 'pro');

-- Metrics table (Renamed to metrics_raw, using TIMESTAMPTZ)
CREATE TABLE metrics_raw (
                             id BIGSERIAL PRIMARY KEY,
                             tenant_id BIGINT NOT NULL REFERENCES tenants(id),
                             metric_name VARCHAR(255) NOT NULL,
                             value DOUBLE PRECISION NOT NULL,
                             timestamp TIMESTAMPTZ NOT NULL,
                             tags JSONB,
                             created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Basic indexes
CREATE INDEX idx_metrics_tenant ON metrics_raw(tenant_id);
CREATE INDEX idx_metrics_timestamp ON metrics_raw(timestamp DESC);
CREATE INDEX idx_metrics_tenant_name_time ON metrics_raw(tenant_id, metric_name, timestamp DESC);