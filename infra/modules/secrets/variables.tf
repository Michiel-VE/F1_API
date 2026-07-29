variable "secret_name" {
  description = "Name for the application secret in AWS Secrets Manager"
  type        = string
  default     = "f1-api/secrets"
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

variable "upstash_redis_token" {
  description = "Upstash Redis REST Token"
  type        = string
  sensitive   = true
}

variable "upstash_redis_url" {
  description = "Upstash Redis REST URL"
  type        = string
}