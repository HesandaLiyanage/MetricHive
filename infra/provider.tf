terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.65"
    }
  }

  # S3 Backend configuration for remote state and DynamoDB state locking on Floci/AWS
  backend "s3" {
    bucket         = "metrichive-tfstate"
    key            = "environments/dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "metrichive-tflock"
    endpoints = {
      s3       = "http://localhost:4566"
      dynamodb = "http://localhost:4566"
    }
    skip_credentials_validation = true
    skip_metadata_api_check     = true
    skip_requesting_account_id  = true
    skip_s3_checksum            = true
    use_path_style              = true
    encrypt                     = true
  }
}

provider "aws" {
  region                      = var.aws_region
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true
  s3_use_path_style           = true

  endpoints {
    apigateway     = "http://localhost:4566"
    cloudwatch     = "http://localhost:4566"
    dynamodb       = "http://localhost:4566"
    ec2            = "http://localhost:4566"
    ecr            = "http://localhost:4566"
    iam            = "http://localhost:4566"
    kms            = "http://localhost:4566"
    rds            = "http://localhost:4566"
    s3             = "http://localhost:4566"
    secretsmanager = "http://localhost:4566"
    sts            = "http://localhost:4566"
  }

  default_tags {
    tags = {
      Project     = "MetricHive"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
