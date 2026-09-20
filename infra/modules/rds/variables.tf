variable "environment" {
  type        = string
  description = "Deployment environment name (e.g. dev, staging, prod)"
  default     = "dev"
}

variable "vpc_id" {
  type        = string
  description = "The VPC ID where the RDS security group will be created"
}

variable "allowed_cidr_blocks" {
  type        = list(string)
  description = "List of CIDR blocks allowed to access the database (e.g. VPC CIDR). Never 0.0.0.0/0."
  default     = []
}

variable "allowed_security_group_ids" {
  type        = list(string)
  description = "List of security group IDs allowed to access the database (e.g. ECS task security group)"
  default     = []
}

variable "db_subnet_group_name" {
  type        = string
  description = "Name of DB subnet group"
}

variable "db_name" {
  type        = string
  description = "Initial database name"
  default     = "metrichive"
}

variable "db_username" {
  type        = string
  description = "Master database username"
  sensitive   = true
}

variable "db_password" {
  type        = string
  description = "Master database password"
  sensitive   = true
}

variable "instance_class" {
  type        = string
  description = "Database instance type (default db.t4g.micro ARM Graviton for cost-performance optimization)"
  default     = "db.t4g.micro"
}

variable "allocated_storage" {
  type        = number
  description = "Allocated storage in gigabytes"
  default     = 20
}

variable "max_allocated_storage" {
  type        = number
  description = "Maximum storage threshold for autoscaling in gigabytes"
  default     = 100
}

variable "engine_version" {
  type        = string
  description = "PostgreSQL engine version"
  default     = "15.7"
}

variable "multi_az" {
  type        = bool
  description = "Specifies if the RDS instance is multi-AZ (false for dev, true for prod)"
  default     = false
}

variable "skip_final_snapshot" {
  type        = bool
  description = "Determines whether a final DB snapshot is created before deleting the instance"
  default     = true
}
