resource "aws_secretsmanager_secret" "app_secrets" {
  name        = var.secret_name
  description = "Application secrets for DB, Google OAuth, and Upstash Redis"
}

resource "aws_secretsmanager_secret_version" "app_secrets_value" {
  secret_id = aws_secretsmanager_secret.app_secrets.id
  secret_string = jsonencode({
    # Database
    db_username          = var.db_username
    db_password          = var.db_password
    url                  = var.db_url

    # Google OAuth & Security
    google_client_id     = var.google_client
    google_client_secret = var.google_client_secret
    jwt_secret_key       = var.jwt_secret_key

    # Upstash Redis
    upstash_redis_token  = var.upstash_redis_token
    upstash_redis_url    = var.upstash_redis_url
  })
}