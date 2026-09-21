# VPC Module: Multi-AZ networking with public, private, and isolated database subnets
module "vpc" {
  source             = "./modules/vpc"
  environment        = var.environment
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
}

# Secrets Manager: Storage for database credentials
resource "aws_secretsmanager_secret" "db_credentials" {
  name                    = "${var.environment}-metrichive-db-credentials"
  description             = "Database credentials for MetricHive PostgreSQL instance"
  recovery_window_in_days = 0

  tags = {
    Name        = "${var.environment}-metrichive-db-credentials"
    Environment = var.environment
  }
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.db_username
    password = var.db_password
    engine   = "postgres"
    host     = module.rds.rds_address
    port     = module.rds.rds_port
    dbname   = var.db_name
  })
}

# IAM Module: ECS task execution and task roles with least-privilege Secrets Manager access
module "iam" {
  source      = "./modules/iam"
  environment = var.environment
  secret_arn  = aws_secretsmanager_secret.db_credentials.arn
}

# ECR Module: Container registry with automated image vulnerability scanning
module "ecr" {
  source          = "./modules/ecr"
  environment     = var.environment
  repository_name = var.ecr_repository_name
}

# RDS Module: PostgreSQL instance using cost-effective db.t4g.micro ARM Graviton instance
module "rds" {
  source               = "./modules/rds"
  environment          = var.environment
  vpc_id               = module.vpc.vpc_id
  db_subnet_group_name = module.vpc.database_subnet_group_name
  allowed_cidr_blocks  = [module.vpc.vpc_cidr_block]
  db_name              = var.db_name
  db_username          = var.db_username
  db_password          = var.db_password
  instance_class       = var.rds_instance_class
  multi_az             = var.rds_multi_az
}

# EC2 Module: Application Host Instance inside the public subnet
module "ec2" {
  source              = "./modules/ec2"
  environment         = var.environment
  vpc_id              = module.vpc.vpc_id
  subnet_id           = module.vpc.public_subnet_ids[0]
  allowed_cidr_blocks = [module.vpc.vpc_cidr_block]
}
