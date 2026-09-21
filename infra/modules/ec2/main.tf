# Security Group for MetricHive EC2 Host
resource "aws_security_group" "ec2" {
  name        = "${var.environment}-metrichive-ec2-sg"
  description = "Security group for MetricHive application host instance"
  vpc_id      = var.vpc_id

  # SSH Management Ingress
  ingress {
    description = "SSH from allowed CIDRs"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  # Application Ingress
  ingress {
    description = "HTTP application ingress for MetricHive API"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  # Outbound Egress
  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.environment}-metrichive-ec2-sg"
    Environment = var.environment
  }
}

# IAM Role and Instance Profile for EC2
resource "aws_iam_role" "ec2_role" {
  name = "${var.environment}-metrichive-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.environment}-metrichive-ec2-role"
    Environment = var.environment
  }
}

resource "aws_iam_role_policy_attachment" "ssm_core" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${var.environment}-metrichive-ec2-profile"
  role = aws_iam_role.ec2_role.name

  lifecycle {
    ignore_changes = [tags, tags_all]
  }
}

# EC2 Application Host Instance
resource "aws_instance" "app_host" {
  ami                    = "ami-0c55b159cbfafe1f0" # Standard Amazon Linux 2 / HVM AMI
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [aws_security_group.ec2.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  user_data = <<-EOF
              #!/bin/bash
              echo "Initializing MetricHive EC2 Application Host"
              yum update -y
              yum install -y docker
              systemctl enable docker
              systemctl start docker
              EOF

  tags = {
    Name        = "${var.environment}-metrichive-app-host"
    Environment = var.environment
    Role        = "ApplicationHost"
  }

  lifecycle {
    ignore_changes = [
      associate_public_ip_address,
      vpc_security_group_ids,
    ]
  }
}
