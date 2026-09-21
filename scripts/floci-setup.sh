#!/usr/bin/env bash
set -euo pipefail

FLOCI_ENDPOINT="http://localhost:4566"
export AWS_ACCESS_KEY_ID="test"
export AWS_SECRET_ACCESS_KEY="test"
export AWS_DEFAULT_REGION="us-east-1"
export AWS_REGION="us-east-1"

echo "=== 1. Checking Floci Container Status ==="
if ! docker ps --format '{{.Names}}' | grep -q "floci"; then
  echo "Starting Floci container..."
  if docker ps -a --format '{{.Names}}' | grep -q "provisr-floci-aws"; then
    docker start provisr-floci-aws
  else
    docker run -d --name floci -p 4566:4566 -v /var/run/docker.sock:/var/run/docker.sock floci/floci:latest
  fi
  sleep 3
fi

echo "Waiting for Floci endpoint at ${FLOCI_ENDPOINT}..."
until curl -s "${FLOCI_ENDPOINT}" > /dev/null; do
  sleep 1
done
echo "Floci is online and listening on port 4566."

echo "=== 2. Bootstrapping S3 State Bucket & DynamoDB Lock Table ==="
if ! aws --endpoint-url="${FLOCI_ENDPOINT}" s3 ls | grep -q "metrichive-tfstate"; then
  echo "Creating S3 bucket: metrichive-tfstate..."
  aws --endpoint-url="${FLOCI_ENDPOINT}" s3 mb s3://metrichive-tfstate
  aws --endpoint-url="${FLOCI_ENDPOINT}" s3api put-bucket-versioning --bucket metrichive-tfstate --versioning-configuration Status=Enabled
fi

if ! aws --endpoint-url="${FLOCI_ENDPOINT}" dynamodb list-tables | grep -q "metrichive-tflock"; then
  echo "Creating DynamoDB table: metrichive-tflock..."
  aws --endpoint-url="${FLOCI_ENDPOINT}" dynamodb create-table \
    --table-name metrichive-tflock \
    --attribute-definitions AttributeName=LockID,AttributeType=S \
    --key-schema AttributeName=LockID,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST
fi

echo "=== 3. Initializing Terraform with S3 Remote Backend & DynamoDB Locking ==="
cd infra
terraform init -reconfigure

echo "=== 4. Executing Speculative Plan (Plan-Driven Provisioning) ==="
terraform plan -var-file=floci.tfvars -out=tfplan

echo "=== 5. Applying Infrastructure Plan ==="
terraform apply "tfplan"

echo "=== Deployment to Floci Completed Successfully ==="
terraform output
