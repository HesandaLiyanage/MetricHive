#!/usr/bin/env bash
set -euo pipefail

FLOCI_ENDPOINT="http://localhost:4566"
export AWS_ACCESS_KEY_ID="test"
export AWS_SECRET_ACCESS_KEY="test"
export AWS_DEFAULT_REGION="us-east-1"
export AWS_REGION="us-east-1"

echo "=== 1. Destroying Terraform Managed AWS Infrastructure on Floci ==="
cd infra
terraform destroy -var-file=floci.tfvars -auto-approve

echo "=== 2. Cleaning up S3 State Bucket & DynamoDB Lock Table ==="
aws --endpoint-url="${FLOCI_ENDPOINT}" s3 rm s3://metrichive-tfstate --recursive || true
aws --endpoint-url="${FLOCI_ENDPOINT}" s3 rb s3://metrichive-tfstate --force || true
aws --endpoint-url="${FLOCI_ENDPOINT}" dynamodb delete-table --table-name metrichive-tflock || true

echo "=== Teardown Completed Successfully ==="
