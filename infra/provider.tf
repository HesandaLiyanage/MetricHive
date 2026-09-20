terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.65"
    }
  }

  # S3 Backend configuration for remote state and DynamoDB state locking.
  # NOTE: This block is intentionally commented out for initial workspace initialization.
  # In a real AWS environment, this requires a pre-existing S3 bucket for state storage
  # and a DynamoDB table for distributed state locking before enabling.
  #
  # backend "s3" {
  #   bucket         = "metrichive-terraform-state-bucket"
  #   key            = "environments/dev/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "metrichive-terraform-locks"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "MetricHive"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
