variable "image_uri" {
  description = "Full ECR image URI including tag"
  type        = string
}

variable "lambda_role_arn" {
  description = "ARN of the IAM role to attach to all Lambda functions"
  type        = string
}

variable "env" {
  description = "Deployment environment (e.g. prod, dev)"
  type        = string
}

variable "app_secret_name" {
  description = "Name of the consolidated secret in AWS Secrets Manager"
  type        = string
}

variable "jwt_secret_key" {
  description = "Secret key used to sign JWT tokens"
  type        = string
  sensitive   = true
}