resource "random_id" "suffix" {
  byte_length = 4
}

resource "aws_secretsmanager_secret" "db_credentials" {
  name = "${var.db_secret_name_prefix}-${random_id.suffix.hex}"
}

resource "aws_secretsmanager_secret_version" "db_credentials_value" {
  secret_id     = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.db_username,
    password = var.db_password,
    url      = var.db_url
  })
}

resource "aws_secretsmanager_secret" "google_oauth_credentials" {
  name = "${var.google_secret_name_prefix}-${random_id.suffix.hex}"
}

resource "aws_secretsmanager_secret_version" "google_oauth_credentials_value" {
  secret_id     = aws_secretsmanager_secret.google_oauth_credentials.id
  secret_string = jsonencode({
    google_client_id     = var.google_client,
    google_client_secret = var.google_client_secret,
    jwt_secret_key       = var.jwt_secret_key
  })
}

resource "aws_secretsmanager_secret" "upstash_credentials" {
  name = "${var.upstash_secret_name_prefix}-${random_id.suffix.hex}"
}

resource "aws_secretsmanager_secret_version" "upstash_credentials_value" {
  secret_id     = aws_secretsmanager_secret.upstash_credentials.id
  secret_string = jsonencode({
    upstash_redis_token = var.upstash_redis_token,
    upstash_redis_url   = var.upstash_redis_url
  })
}