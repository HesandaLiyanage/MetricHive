-- Insert tenants first (since metrics reference tenant_id)
INSERT INTO tenants (id, name, email, api_key, tier, max_metrics_per_day, max_requests_per_minute, created_at, updated_at)
VALUES
    (1, 'Development Tenant', 'dev@example.com', 'dev-api-key-123456789', 'premium', 100000, 1000, NOW(), NOW()),
    (2, 'Test Tenant', 'test@example.com', 'test-api-key-abcdef123', 'free', 10000, 60, NOW(), NOW()),
    (3, 'Staging Tenant', 'staging@example.com', 'stage-api-key-xyz789', 'pro', 50000, 500, NOW(), NOW())
    ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
                            email = EXCLUDED.email,
                            api_key = EXCLUDED.api_key,
                            updated_at = NOW();

-- Insert sample metrics
INSERT INTO metrics (id, tenant_id, metric_name, value, timestamp, tags, created_at)
VALUES
    (1, 1, 'cpu_usage', 75.5, '2026-02-11 10:30:00', '{"host": "server-01", "region": "us-east"}', NOW()),
    (2, 1, 'memory_usage', 62.3, '2026-02-11 10:30:00', '{"host": "server-01", "region": "us-east"}', NOW()),
    (3, 1, 'disk_usage', 45.2, '2026-02-11 10:30:00', '{"host": "server-01", "region": "us-east"}', NOW()),
    (4, 2, 'http_requests_per_second', 125.0, '2026-02-11 10:31:00', '{"endpoint": "/api/users", "method": "GET"}', NOW()),
    (5, 2, 'response_time_ms', 142.5, '2026-02-11 10:31:00', '{"endpoint": "/api/users", "method": "GET"}', NOW()),
    (6, 3, 'database_connections', 23.0, '2026-02-11 10:32:00', '{"database": "main_db", "pool": "read_replica"}', NOW()),
    (7, 1, 'cpu_usage', 78.1, '2026-02-11 10:31:00', '{"host": "server-02", "region": "us-west"}', NOW()),
    (8, 1, 'network_incoming_bytes', 1024000.0, '2026-02-11 10:32:00', '{"interface": "eth0", "direction": "incoming"}', NOW()),
    (9, 2, 'error_rate_percentage', 0.5, '2026-02-11 10:33:00', '{"service": "auth-service", "environment": "testing"}', NOW()),
    (10, 3, 'queue_size', 42.0, '2026-02-11 10:34:00', '{"queue": "email_queue", "worker": "worker-01"}', NOW())
    ON CONFLICT (id) DO UPDATE SET
                            value = EXCLUDED.value,
                            timestamp = EXCLUDED.timestamp,
                            updated_at = NOW();