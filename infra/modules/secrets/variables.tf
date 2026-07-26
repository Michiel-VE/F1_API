variable "db_secret_name_prefix" {
  description = "Prefix for the DB Secrets Manager secret name"
  type        = string
}

variable "google_secret_name_prefix" {
  description = "Prefix for the Google OAuth Secrets Manager secret name"
  type        = string
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