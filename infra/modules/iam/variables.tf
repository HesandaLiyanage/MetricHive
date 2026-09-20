variable "environment" {
  type        = string
  description = "Deployment environment name"
  default     = "dev"
}

variable "secret_arn" {
  type        = string
  description = "ARN of the Secrets Manager secret for DB credentials (scoped policy target, no wildcards)"
}
