resource "aws_api_gateway_rest_api" "f1_api" {
  name               = "f1-api-gateway"
  endpoint_configuration { types = ["REGIONAL"] }
  binary_media_types = ["*/*"]
}

resource "aws_api_gateway_resource" "api" {
  rest_api_id = aws_api_gateway_rest_api.f1_api.id
  parent_id   = aws_api_gateway_rest_api.f1_api.root_resource_id
  path_part   = "api"
}

resource "aws_api_gateway_resource" "v1" {
  rest_api_id = aws_api_gateway_rest_api.f1_api.id
  parent_id   = aws_api_gateway_resource.api.id
  path_part   = "v1"
}

resource "aws_api_gateway_resource" "v1_proxy" {
  rest_api_id = aws_api_gateway_rest_api.f1_api.id
  parent_id   = aws_api_gateway_resource.v1.id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_resource" "login" {
  rest_api_id = aws_api_gateway_rest_api.f1_api.id
  parent_id   = aws_api_gateway_rest_api.f1_api.root_resource_id
  path_part   = "login"
}

resource "aws_api_gateway_resource" "login_oauth2" {
  rest_api_id = aws_api_gateway_rest_api.f1_api.id
  parent_id   = aws_api_gateway_resource.login.id
  path_part   = "oauth2"
}

resource "aws_api_gateway_resource" "login_oauth2_proxy" {
  rest_api_id = aws_api_gateway_rest_api.f1_api.id
  parent_id   = aws_api_gateway_resource.login_oauth2.id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_resource" "root_oauth2" {
  rest_api_id = aws_api_gateway_rest_api.f1_api.id
  parent_id   = aws_api_gateway_rest_api.f1_api.root_resource_id
  path_part   = "oauth2"
}

resource "aws_api_gateway_resource" "root_oauth2_proxy" {
  rest_api_id = aws_api_gateway_rest_api.f1_api.id
  parent_id   = aws_api_gateway_resource.root_oauth2.id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "v1_proxy_any" {
  rest_api_id   = aws_api_gateway_rest_api.f1_api.id
  resource_id   = aws_api_gateway_resource.v1_proxy.id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "v1_proxy_int" {
  rest_api_id             = aws_api_gateway_rest_api.f1_api.id
  resource_id             = aws_api_gateway_resource.v1_proxy.id
  http_method             = aws_api_gateway_method.v1_proxy_any.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = var.lambda_alias_invoke_arn
}

resource "aws_api_gateway_method" "login_oauth_proxy_any" {
  rest_api_id   = aws_api_gateway_rest_api.f1_api.id
  resource_id   = aws_api_gateway_resource.login_oauth2_proxy.id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "login_oauth_proxy_int" {
  rest_api_id             = aws_api_gateway_rest_api.f1_api.id
  resource_id             = aws_api_gateway_resource.login_oauth2_proxy.id
  http_method             = aws_api_gateway_method.login_oauth_proxy_any.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = var.lambda_alias_invoke_arn
}

resource "aws_api_gateway_method" "root_oauth_proxy_any" {
  rest_api_id   = aws_api_gateway_rest_api.f1_api.id
  resource_id   = aws_api_gateway_resource.root_oauth2_proxy.id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "root_oauth_proxy_int" {
  rest_api_id             = aws_api_gateway_rest_api.f1_api.id
  resource_id             = aws_api_gateway_resource.root_oauth2_proxy.id
  http_method             = aws_api_gateway_method.root_oauth_proxy_any.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = var.lambda_alias_invoke_arn
}

resource "aws_api_gateway_method" "login_root_any" {
  rest_api_id   = aws_api_gateway_rest_api.f1_api.id
  resource_id   = aws_api_gateway_resource.login.id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "login_root_int" {
  rest_api_id             = aws_api_gateway_rest_api.f1_api.id
  resource_id             = aws_api_gateway_resource.login.id
  http_method             = aws_api_gateway_method.login_root_any.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = var.lambda_alias_invoke_arn
}

resource "aws_api_gateway_gateway_response" "unauthorized_401" {
  rest_api_id   = aws_api_gateway_rest_api.f1_api.id
  response_type = "UNAUTHORIZED"
  status_code   = "401"

  response_parameters = {
    "gatewayresponse.header.Access-Control-Allow-Origin"      = "'https://f1.michielve.be'"
    "gatewayresponse.header.Access-Control-Allow-Credentials" = "'true'"
  }
}

resource "aws_api_gateway_gateway_response" "access_denied_403" {
  rest_api_id   = aws_api_gateway_rest_api.f1_api.id
  response_type = "ACCESS_DENIED"
  status_code   = "403"

  response_parameters = {
    "gatewayresponse.header.Access-Control-Allow-Origin"      = "'https://f1.michielve.be'"
    "gatewayresponse.header.Access-Control-Allow-Credentials" = "'true'"
  }
}

resource "aws_api_gateway_gateway_response" "resource_not_found_404" {
  rest_api_id   = aws_api_gateway_rest_api.f1_api.id
  response_type = "RESOURCE_NOT_FOUND"
  status_code   = "404"

  response_parameters = {
    "gatewayresponse.header.Access-Control-Allow-Origin"      = "'https://f1.michielve.be'"
    "gatewayresponse.header.Access-Control-Allow-Credentials" = "'true'"
  }
}

resource "aws_lambda_permission" "allow_api_gateway" {
  action        = "lambda:InvokeFunction"
  function_name = var.lambda_alias_arn
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.f1_api.execution_arn}/*/*"
}

resource "aws_api_gateway_deployment" "f1_api_deployment" {
  rest_api_id = aws_api_gateway_rest_api.f1_api.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_integration.v1_proxy_int.id,
      aws_api_gateway_integration.login_oauth_proxy_int.id,
      aws_api_gateway_integration.root_oauth_proxy_int.id,
      aws_api_gateway_integration.login_root_int.id,
      aws_api_gateway_gateway_response.unauthorized_401.id,
      aws_api_gateway_gateway_response.access_denied_403.id,
      aws_api_gateway_gateway_response.resource_not_found_404.id,
      var.lambda_alias_invoke_arn
    ]))
  }

  lifecycle { create_before_destroy = true }

  depends_on = [
    aws_api_gateway_integration.v1_proxy_int,
    aws_api_gateway_integration.login_oauth_proxy_int,
    aws_api_gateway_integration.root_oauth_proxy_int,
    aws_api_gateway_integration.login_root_int,
    aws_api_gateway_gateway_response.unauthorized_401,
    aws_api_gateway_gateway_response.access_denied_403,
    aws_api_gateway_gateway_response.resource_not_found_404
  ]
}

resource "aws_api_gateway_stage" "prod" {
  deployment_id = aws_api_gateway_deployment.f1_api_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.f1_api.id
  stage_name    = "prod"
}