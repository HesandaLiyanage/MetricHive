resource "aws_security_group" "rds" {
  name        = "${var.environment}-metrichive-rds-sg"
  description = "Controls database access - strictly internal to VPC and ECS"
  vpc_id      = var.vpc_id

  tags = {
    Name        = "${var.environment}-metrichive-rds-sg"
    Environment = var.environment
  }
}

resource "aws_security_group_rule" "rds_ingress_cidr" {
  count             = length(var.allowed_cidr_blocks) > 0 ? 1 : 0
  type              = "ingress"
  from_port         = 5432
  to_port           = 5432
  protocol          = "tcp"
  cidr_blocks       = var.allowed_cidr_blocks
  security_group_id = aws_security_group.rds.id
  description       = "Allow PostgreSQL access strictly from authorized internal CIDRs (no 0.0.0.0/0)"
}

resource "aws_security_group_rule" "rds_ingress_sg" {
  count                    = length(var.allowed_security_group_ids) > 0 ? length(var.allowed_security_group_ids) : 0
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = var.allowed_security_group_ids[count.index]
  security_group_id        = aws_security_group.rds.id
  description              = "Allow PostgreSQL access strictly from ECS task security group"
}

resource "aws_security_group_rule" "rds_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.rds.id
  description       = "Allow outbound traffic for updates/synchronization"
}

resource "aws_db_instance" "postgres" {
  identifier                 = "${var.environment}-metrichive-pg"
  engine                     = "postgres"
  engine_version             = var.engine_version
  instance_class             = var.instance_class
  allocated_storage          = var.allocated_storage
  max_allocated_storage      = var.max_allocated_storage
  storage_type               = "gp3"
  storage_encrypted          = true
  db_name                    = var.db_name
  username                   = var.db_username
  password                   = var.db_password
  db_subnet_group_name       = var.db_subnet_group_name
  vpc_security_group_ids     = [aws_security_group.rds.id]
  multi_az                   = var.multi_az
  publicly_accessible        = false
  skip_final_snapshot        = var.skip_final_snapshot
  auto_minor_version_upgrade = true
  apply_immediately          = true

  tags = {
    Name        = "${var.environment}-metrichive-postgres"
    Environment = var.environment
  }
}
