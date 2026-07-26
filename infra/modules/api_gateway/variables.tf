variable "lambda_alias_arn" {
  description = "ARN of the main Lambda alias (used for API Gateway permission)"
  type        = string
}

variable "lambda_alias_invoke_arn" {
  description = "Invoke ARN of the main Lambda alias (used for API Gateway integrations)"
  type        = string
}