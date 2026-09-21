variable "environment" {
  type        = string
  description = "Deployment environment name (e.g. dev, staging, prod)"
}

variable "vpc_id" {
  type        = string
  description = "VPC ID where the EC2 instance and security group will reside"
}

variable "subnet_id" {
  type        = string
  description = "Subnet ID (public subnet) where the EC2 instance will be launched"
}

variable "allowed_cidr_blocks" {
  type        = list(string)
  description = "CIDR blocks permitted for SSH and management access"
  default     = ["10.0.0.0/16"]
}

variable "instance_type" {
  type        = string
  description = "EC2 instance type"
  default     = "t3.micro"
}
