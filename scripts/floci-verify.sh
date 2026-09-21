#!/usr/bin/env bash
set -euo pipefail

FLOCI_ENDPOINT="http://localhost:4566"
export AWS_ACCESS_KEY_ID="test"
export AWS_SECRET_ACCESS_KEY="test"
export AWS_DEFAULT_REGION="us-east-1"
export AWS_REGION="us-east-1"

echo "=== 1. Checking Remote State in S3 & DynamoDB ==="
aws --endpoint-url="${FLOCI_ENDPOINT}" s3 ls s3://metrichive-tfstate/environments/dev/
aws --endpoint-url="${FLOCI_ENDPOINT}" dynamodb scan --table-name metrichive-tflock

echo "=== 2. Checking Provisioned VPC and Subnets ==="
aws --endpoint-url="${FLOCI_ENDPOINT}" ec2 describe-vpcs --filters "Name=tag:Name,Values=dev-metrichive-vpc"
aws --endpoint-url="${FLOCI_ENDPOINT}" ec2 describe-subnets --filters "Name=vpc-id,Values=$(cd infra && terraform output -raw vpc_id)" --query "Subnets[*].{ID:SubnetId,CIDR:CidrBlock,Zone:AvailabilityZone}"

echo "=== 3. Checking Provisioned EC2 Instance ==="
aws --endpoint-url="${FLOCI_ENDPOINT}" ec2 describe-instances --filters "Name=tag:Name,Values=dev-metrichive-app-host" --query "Reservations[*].Instances[*].{ID:InstanceId,State:State.Name,IP:PrivateIpAddress,Subnet:SubnetId}"

echo "=== 4. Checking Provisioned RDS PostgreSQL Instance ==="
aws --endpoint-url="${FLOCI_ENDPOINT}" rds describe-db-instances --db-instance-identifier "dev-metrichive-pg" --query "DBInstances[*].{ID:DBInstanceIdentifier,Engine:Engine,Class:DBInstanceClass,Status:DBInstanceStatus,Endpoint:Endpoint.Address,Port:Endpoint.Port}"

echo "=== 5. Checking Provisioned ECR Repository ==="
aws --endpoint-url="${FLOCI_ENDPOINT}" ecr describe-repositories --repository-names "metrichive" --query "repositories[*].{Name:repositoryName,URI:repositoryUri}"

echo "=== 6. Checking Secrets Manager Database Credentials ==="
aws --endpoint-url="${FLOCI_ENDPOINT}" secretsmanager get-secret-value --secret-id "dev-metrichive-db-credentials" --query "SecretString" --output text

echo "=== 7. Proving Zero Drift via Plan Detailed Exitcode ==="
cd infra
if terraform plan -var-file=floci.tfvars -detailed-exitcode; then
  echo "SUCCESS: Zero drift detected! Real infrastructure strictly matches Terraform configuration."
else
  echo "WARNING: Drift or diff detected."
fi
