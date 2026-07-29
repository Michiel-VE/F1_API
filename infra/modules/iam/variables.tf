variable "ecr_repository_arn" {
  description = "ARN of the ECR repository the Lambda needs pull access to"
  type        = string
}

variable "secret_arn" {
  description = "ARN of the consolidated application secret in AWS Secrets Manager"
  type        = string
}