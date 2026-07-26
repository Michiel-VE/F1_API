variable "ecr_repository_arn" {
  description = "ARN of the ECR repository the Lambda needs pull access to"
  type        = string
}

variable "db_secret_arn" {
  description = "ARN of the DB credentials secret"
  type        = string
}

variable "google_secret_arn" {
  description = "ARN of the Google OAuth credentials secret"
  type        = string
}

variable "upstash_secret_arn" {
  description = "ARN for the Upstash secret"
  type        = string
}