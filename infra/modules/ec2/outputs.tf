output "instance_id" {
  value       = aws_instance.app_host.id
  description = "ID of the provisioned EC2 instance"
}

output "public_ip" {
  value       = aws_instance.app_host.public_ip
  description = "Public IPv4 address of the EC2 instance"
}

output "private_ip" {
  value       = aws_instance.app_host.private_ip
  description = "Private IPv4 address of the EC2 instance"
}

output "security_group_id" {
  value       = aws_security_group.ec2.id
  description = "ID of the EC2 instance security group"
}
