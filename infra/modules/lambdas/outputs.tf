output "lambda_alias_arn" {
  value = aws_lambda_alias.f1_lambda_alias.arn
}

output "lambda_alias_invoke_arn" {
  value = aws_lambda_alias.f1_lambda_alias.invoke_arn
}