output "vpc_id" {
  description = "The ID of the provisioned VPC"
  value       = module.vpc.vpc_id
}

output "rds_endpoint" {
  description = "Connection endpoint of the PostgreSQL RDS instance (host:port)"
  value       = module.rds.rds_endpoint
}

output "ecr_repository_url" {
  description = "URL of the MetricHive ECR repository"
  value       = module.ecr.repository_url
}

output "ec2_instance_id" {
  description = "ID of the MetricHive EC2 application host"
  value       = module.ec2.instance_id
}

output "ec2_public_ip" {
  description = "Public IP address of the MetricHive EC2 application host"
  value       = module.ec2.public_ip
}
