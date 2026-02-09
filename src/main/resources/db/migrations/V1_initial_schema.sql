-- Tenants table
CREATE TABLE tenants (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         api_key VARCHAR(255) UNIQUE NOT NULL,
                         tier VARCHAR(50) DEFAULT 'free',
                         max_metrics_per_day INT DEFAULT 10000,
                         max_requests_per_minute INT DEFAULT 60,
                         created_at TIMESTAMP DEFAULT NOW(),
                         updated_at TIMESTAMP DEFAULT NOW()
);

-- Insert a test tenant for development
INSERT INTO tenants (name, email, api_key, tier)
VALUES ('Test Tenant', 'test@metrichive.com', 'test_key_12345', 'pro');

-- Metrics table (we'll add partitioning later)
CREATE TABLE metrics (
                         id BIGSERIAL PRIMARY KEY,
                         tenant_id BIGINT NOT NULL REFERENCES tenants(id),
                         metric_name VARCHAR(255) NOT NULL,
                         value DOUBLE PRECISION NOT NULL,
                         timestamp TIMESTAMP NOT NULL,
                         tags JSONB,
                         created_at TIMESTAMP DEFAULT NOW()
);

-- Basic indexes (we'll optimize later)
CREATE INDEX idx_metrics_tenant ON metrics(tenant_id);
CREATE INDEX idx_metrics_timestamp ON metrics(timestamp DESC);
CREATE INDEX idx_metrics_tenant_name_time ON metrics(tenant_id, metric_name, timestamp DESC);