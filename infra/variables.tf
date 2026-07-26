variable "db_secret_name_prefix" {
  description = "Prefix for the Secrets Manager secret name"
  type        = string
  default     = "db_credentials"
}

variable "db_username" {
  description = "Database username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "db_url" {
  description = "Database connection URL"
  type        = string
}

variable "aws_region" {
  description = "AWS region to deploy resources in"
  type        = string
  default     = "eu-north-1"
}

variable "env" {
  description = "Deployment environment (e.g. prod, dev)"
  type        = string
  default     = "prod"
}

# Google OAuth Secrets
variable "google_secret_name_prefix" {
  description = "Prefix for the Google OAuth Secrets Manager secret name"
  type        = string
  default     = "google_oauth_credentials"
}

variable "google_client" {
  description = "Google OAuth Client ID"
  type        = string
  sensitive   = true
}

variable "google_client_secret" {
  description = "Google OAuth Client Secret"
  type        = string
  sensitive   = true
}

variable "jwt_secret_key" {
  description = "Secret key used to sign JWT tokens"
  type        = string
  sensitive   = true
}

variable "aws_account_id" {
  description = "Your 12-digit AWS account ID (find it in the AWS Console top-right menu)"
  type        = string
}

variable "image_tag" {
  description = "Set automatically by deploy.ps1"
  type        = string
}

variable "upstash_secret_name_prefix" {
  description = "Prefix for the Upstash Redis Secrets Manager secret name"
  type        = string
  default     = "f1-api/upstash"
}

variable "upstash_redis_token" {
  description = "Upstash Redis REST Token"
  type        = string
  sensitive   = true
}

variable "upstash_redis_url" {
  description = "Upstash Redis REST URL"
  type        = string
}