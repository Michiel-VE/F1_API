output "db_secret_arn" {
  value = aws_secretsmanager_secret.db_credentials.arn
}

output "db_secret_name" {
  value = aws_secretsmanager_secret.db_credentials.name
}

output "google_secret_arn" {
  value = aws_secretsmanager_secret.google_oauth_credentials.arn
}

output "google_secret_name" {
  value = aws_secretsmanager_secret.google_oauth_credentials.name
}

output "upstash_secret_arn" {
  value = aws_secretsmanager_secret.upstash_credentials.arn
}

output "upstash_secret_name" {
  value = aws_secretsmanager_secret.upstash_credentials.name
}