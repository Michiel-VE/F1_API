# ---------------------------------------------------------------------------
# CloudWatch Log Groups
# ---------------------------------------------------------------------------

resource "aws_cloudwatch_log_group" "f1_lambda_logs" {
  name              = "/aws/lambda/f1-api-lambda"
  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "race_updater_logs" {
  name              = "/aws/lambda/f1-api-race-updater"
  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "driver_team_logs" {
  name              = "/aws/lambda/f1-api-driver-team-updater"
  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "standing_logs" {
  name              = "/aws/lambda/f1-api-standing-updater"
  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "race_result_logs" {
  name              = "/aws/lambda/f1-api-race-result-updater"
  retention_in_days = 30
}

# ---------------------------------------------------------------------------
# Main API Lambda
# ---------------------------------------------------------------------------

resource "aws_lambda_function" "f1_lambda" {
  function_name = "f1-api-lambda"
  role          = var.lambda_role_arn
  package_type  = "Image"
  image_uri     = var.image_uri
  timeout       = 60
  memory_size   = 2048
  publish       = true

  image_config {
    command = ["be.michielve.f1_api.lambdas.LambdaHandler::handleRequest"]
  }

  environment {
    variables = {
      ENV                                                          = var.env
      DB_SECRET_NAME                                               = var.db_secret_name
      GOOGLE_SECRET_NAME                                           = var.google_secret_name
      UPSTASH_SECRET_NAME                                          = var.upstash_secret_name
      JWT_SECRET_KEY                                               = var.jwt_secret_key
      SPRING_FLYWAY_BASELINE_ON_MIGRATE                            = "true"
      BASE_URL                                                     = "https://f1-api.michielve.be"
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_REDIRECT_URI = "https://f1-api.michielve.be/login/oauth2/code/google"
      FRONTEND_URL                                                 = "https://f1.michielve.be"
      SERVER_FORWARD_HEADERS_STRATEGY                              = "native"
      SERVER_SESSION_COOKIE_SECURE                                 = "true"
      SERVER_SESSION_COOKIE_HTTP_ONLY                              = "true"
      JAVA_TOOL_OPTIONS                                            = "-XX:TieredStopAtLevel=1"
    }
  }

  depends_on = [aws_cloudwatch_log_group.f1_lambda_logs]
}

resource "aws_lambda_alias" "f1_lambda_alias" {
  name             = var.env
  function_name    = aws_lambda_function.f1_lambda.function_name
  function_version = aws_lambda_function.f1_lambda.version
}

# ---------------------------------------------------------------------------
# Warmer
# ---------------------------------------------------------------------------

resource "aws_cloudwatch_event_rule" "api_warmer" {
  name                = "f1-api-warmer"
  description         = "Keeps main API Lambda warm to avoid cold starts"
  schedule_expression = "rate(10 minutes)"
}

resource "aws_cloudwatch_event_target" "api_warmer_target" {
  rule      = aws_cloudwatch_event_rule.api_warmer.name
  target_id = "api-warmer"
  arn       = aws_lambda_function.f1_lambda.arn
}

resource "aws_lambda_permission" "allow_warmer_eventbridge" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.f1_lambda.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.api_warmer.arn
}

# ---------------------------------------------------------------------------
# Race Updater
# ---------------------------------------------------------------------------

resource "aws_lambda_function" "race_scheduler" {
  function_name = "f1-api-race-updater"
  role          = var.lambda_role_arn
  package_type  = "Image"
  image_uri     = var.image_uri
  timeout       = 60
  memory_size   = 1024

  image_config {
    command = ["be.michielve.f1_api.lambdas.RaceUpdateLambdaHandler::handleRequest"]
  }

  environment {
    variables = {
      ENV                                 = var.env
      DB_SECRET_NAME                      = var.db_secret_name
      GOOGLE_SECRET_NAME                  = var.google_secret_name
      UPSTASH_SECRET_NAME                 = var.upstash_secret_name
      SPRING_FLYWAY_BASELINE_ON_MIGRATE   = "true"
      JAVA_TOOL_OPTIONS                   = "-XX:TieredStopAtLevel=1"
    }
  }

  depends_on = [aws_cloudwatch_log_group.race_updater_logs]
}

resource "aws_cloudwatch_event_rule" "race_schedule" {
  name                = "f1-api-race-schedule"
  schedule_expression = "cron(0 3 ? * WED *)"
}

resource "aws_cloudwatch_event_target" "race_target" {
  rule      = aws_cloudwatch_event_rule.race_schedule.name
  target_id = "race-updater"
  arn       = aws_lambda_function.race_scheduler.arn
}

resource "aws_lambda_permission" "allow_race_eventbridge" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.race_scheduler.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.race_schedule.arn
}

# ---------------------------------------------------------------------------
# Driver/Team Updater
# ---------------------------------------------------------------------------

resource "aws_lambda_function" "driver_team_update" {
  function_name = "f1-api-driver-team-updater"
  role          = var.lambda_role_arn
  package_type  = "Image"
  image_uri     = var.image_uri
  timeout       = 60
  memory_size   = 1024

  image_config {
    command = ["be.michielve.f1_api.lambdas.DriverTeamUpdateLambdaHandler::handleRequest"]
  }

  environment {
    variables = {
      ENV                                 = var.env
      DB_SECRET_NAME                      = var.db_secret_name
      GOOGLE_SECRET_NAME                  = var.google_secret_name
      UPSTASH_SECRET_NAME                 = var.upstash_secret_name
      SPRING_FLYWAY_BASELINE_ON_MIGRATE   = "true"
      JAVA_TOOL_OPTIONS                   = "-XX:TieredStopAtLevel=1"
    }
  }

  depends_on = [aws_cloudwatch_log_group.driver_team_logs]
}

resource "aws_cloudwatch_event_rule" "driver_team_schedule" {
  name                = "f1-api-driver-team-schedule"
  schedule_expression = "cron(5 3 ? * WED *)"
}

resource "aws_cloudwatch_event_target" "driver_team_target" {
  rule      = aws_cloudwatch_event_rule.driver_team_schedule.name
  target_id = "driver-team-updater"
  arn       = aws_lambda_function.driver_team_update.arn
}

resource "aws_lambda_permission" "allow_driver_team_eventbridge" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.driver_team_update.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.driver_team_schedule.arn
}

# ---------------------------------------------------------------------------
# Standings Updater
# ---------------------------------------------------------------------------

resource "aws_lambda_function" "standing_scheduler" {
  function_name = "f1-api-standing-updater"
  role          = var.lambda_role_arn
  package_type  = "Image"
  image_uri     = var.image_uri
  timeout       = 60
  memory_size   = 1024

  image_config {
    command = ["be.michielve.f1_api.lambdas.StandingUpdateLambdaHandler::handleRequest"]
  }

  environment {
    variables = {
      ENV                                 = var.env
      DB_SECRET_NAME                      = var.db_secret_name
      GOOGLE_SECRET_NAME                  = var.google_secret_name
      UPSTASH_SECRET_NAME                 = var.upstash_secret_name
      SPRING_FLYWAY_BASELINE_ON_MIGRATE   = "true"
      JAVA_TOOL_OPTIONS                   = "-XX:TieredStopAtLevel=1"
    }
  }

  depends_on = [aws_cloudwatch_log_group.standing_logs]
}

resource "aws_cloudwatch_event_rule" "standing_schedule" {
  name                = "f1-api-standing-schedule"
  schedule_expression = "cron(10 3 ? * SAT,SUN,MON *)"
}

resource "aws_cloudwatch_event_target" "standing_target" {
  rule      = aws_cloudwatch_event_rule.standing_schedule.name
  target_id = "standing-updater"
  arn       = aws_lambda_function.standing_scheduler.arn
}

resource "aws_lambda_permission" "allow_standing_eventbridge" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.standing_scheduler.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.standing_schedule.arn
}

# ---------------------------------------------------------------------------
# Race Result Updater
# ---------------------------------------------------------------------------

resource "aws_lambda_function" "race_result_scheduler" {
  function_name = "f1-api-race-result-updater"
  role          = var.lambda_role_arn
  package_type  = "Image"
  image_uri     = var.image_uri
  timeout       = 60
  memory_size   = 1024

  image_config {
    command = ["be.michielve.f1_api.lambdas.RaceResultUpdateLambdaHandler::handleRequest"]
  }

  environment {
    variables = {
      ENV                                 = var.env
      DB_SECRET_NAME                      = var.db_secret_name
      GOOGLE_SECRET_NAME                  = var.google_secret_name
      UPSTASH_SECRET_NAME                 = var.upstash_secret_name
      SPRING_FLYWAY_BASELINE_ON_MIGRATE   = "true"
      JAVA_TOOL_OPTIONS                   = "-XX:TieredStopAtLevel=1"
    }
  }

  depends_on = [aws_cloudwatch_log_group.race_result_logs]
}

resource "aws_cloudwatch_event_rule" "race_result_schedule" {
  name                = "f1-api-race-result-schedule"
  schedule_expression = "cron(0 3 ? * MON *)"
}

resource "aws_cloudwatch_event_target" "race_result_target" {
  rule      = aws_cloudwatch_event_rule.race_result_schedule.name
  target_id = "race-result-updater"
  arn       = aws_lambda_function.race_result_scheduler.arn
}

resource "aws_lambda_permission" "allow_race_result_eventbridge" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.race_result_scheduler.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.race_result_schedule.arn
}