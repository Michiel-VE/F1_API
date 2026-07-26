output "rest_api_id" {
  value = aws_api_gateway_rest_api.f1_api.id
}

output "stage_name" {
  value = aws_api_gateway_stage.prod.stage_name
}