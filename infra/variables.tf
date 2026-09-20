variable "aws_region" {
  type        = string
  description = "AWS region for infrastructure deployment"
  default     = "us-east-1"
}

variable "environment" {
  type        = string
  description = "Deployment environment name (e.g. dev, staging, prod)"
  default     = "dev"
}

variable "vpc_cidr" {
  type        = string
  description = "CIDR block for the VPC"
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  type        = list(string)
  description = "Availability zones for high-availability subnet distribution"
  default     = ["us-east-1a", "us-east-1b"]
}

variable "db_name" {
  type        = string
  description = "Initial database name for PostgreSQL"
  default     = "metrichive"
}

variable "db_username" {
  type        = string
  description = "Master database username (sensitive, no default value)"
  sensitive   = true
}

variable "db_password" {
  type        = string
  description = "Master database password (sensitive, no default value)"
  sensitive   = true
}

variable "rds_instance_class" {
  type        = string
  description = "Database instance type (default db.t4g.micro ARM Graviton for cost-performance efficiency)"
  default     = "db.t4g.micro"
}

variable "rds_multi_az" {
  type        = bool
  description = "Specifies whether the RDS instance is multi-AZ (false for dev, true for prod)"
  default     = false
}

variable "ecr_repository_name" {
  type        = string
  description = "Name for the application ECR repository"
  default     = "metrichive"
}
