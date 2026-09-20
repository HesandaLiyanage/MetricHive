# Trust relationship policy allowing ECS Tasks service to assume roles
data "aws_iam_policy_document" "ecs_tasks_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

# ECS Task Execution Role (used by ECS agent to pull ECR images and fetch secrets)
resource "aws_iam_role" "ecs_execution_role" {
  name               = "${var.environment}-metrichive-ecs-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json

  tags = {
    Name        = "${var.environment}-metrichive-ecs-execution-role"
    Environment = var.environment
  }
}

# Managed policy for standard ECS execution (logs, ECR pull)
resource "aws_iam_role_policy_attachment" "ecs_execution_standard" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Strictly scoped read-only policy for Secrets Manager (no wildcards)
data "aws_iam_policy_document" "secrets_read_policy_doc" {
  statement {
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret"
    ]
    resources = [
      var.secret_arn
    ]
  }
}

resource "aws_iam_role_policy" "secrets_read" {
  name   = "${var.environment}-metrichive-secrets-read"
  role   = aws_iam_role.ecs_execution_role.id
  policy = data.aws_iam_policy_document.secrets_read_policy_doc.json
}

# ECS Task Role (used by running container for runtime AWS API calls)
resource "aws_iam_role" "ecs_task_role" {
  name               = "${var.environment}-metrichive-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json

  tags = {
    Name        = "${var.environment}-metrichive-ecs-task-role"
    Environment = var.environment
  }
}
