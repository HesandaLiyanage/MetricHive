output "rds_endpoint" {
  description = "Connection endpoint of the PostgreSQL RDS instance (host:port)"
  value       = aws_db_instance.postgres.endpoint
}

output "rds_address" {
  description = "Hostname of the PostgreSQL RDS instance"
  value       = aws_db_instance.postgres.address
}

output "rds_port" {
  description = "Port the database listens on"
  value       = aws_db_instance.postgres.port
}

output "security_group_id" {
  description = "ID of the RDS security group"
  value       = aws_security_group.rds.id
}

output "db_name" {
  description = "Database name"
  value       = aws_db_instance.postgres.db_name
}
