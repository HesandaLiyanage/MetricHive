variable "repository_name" {
  type        = string
  description = "Name of the ECR repository"
  default     = "metrichive"
}

variable "image_tag_mutability" {
  type        = string
  description = "The tag mutability setting for the repository (MUTABLE or IMMUTABLE)"
  default     = "MUTABLE"
}

variable "environment" {
  type        = string
  description = "Deployment environment name"
  default     = "dev"
}

variable "max_image_count" {
  type        = number
  description = "Number of tagged images to keep before pruning"
  default     = 30
}
