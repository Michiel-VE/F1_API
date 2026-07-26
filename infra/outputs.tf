output "lambda_function_name" {
  value = module.lambdas.lambda_alias_arn
  description = "ARN of the main Lambda alias"
}

output "db_secret_arn" {
  value = module.secrets.db_secret_arn
}

output "db_secret_name" {
  value = module.secrets.db_secret_name
}

output "api_url" {
  value = "https://${module.api_gateway.rest_api_id}.execute-api.${var.aws_region}.amazonaws.com/${module.api_gateway.stage_name}/api/v1"
}