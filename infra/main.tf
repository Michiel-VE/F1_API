locals {
  ecr_base  = "${var.aws_account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
  image_uri = "${var.aws_account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/f1-api:latest"
}

provider "aws" {
  region = var.aws_region
}

module "ecr" {
  source = "./modules/ecr"
}

module "iam" {
  source             = "./modules/iam"
  ecr_repository_arn = module.ecr.repository_arn
  secret_arn         = module.secrets.secret_arn
}

module "secrets" {
  source               = "./modules/secrets"
  secret_name          = var.secret_name
  db_username          = var.db_username
  db_password          = var.db_password
  db_url               = var.db_url
  google_client        = var.google_client
  google_client_secret = var.google_client_secret
  jwt_secret_key       = var.jwt_secret_key
  upstash_redis_token  = var.upstash_redis_token
  upstash_redis_url    = var.upstash_redis_url
}

module "lambdas" {
  source          = "./modules/lambdas"
  image_uri       = local.image_uri
  lambda_role_arn = module.iam.lambda_role_arn
  env             = var.env
  app_secret_name = module.secrets.secret_name
  jwt_secret_key  = var.jwt_secret_key
}

module "api_gateway" {
  source                  = "./modules/api_gateway"
  lambda_alias_arn        = module.lambdas.lambda_alias_arn
  lambda_alias_invoke_arn = module.lambdas.lambda_alias_invoke_arn
}

terraform {
  backend "s3" {
    bucket         = "f1-api-tfstate"
    key            = "f1-api/terraform.tfstate"
    region         = "eu-north-1"
    dynamodb_table = "f1-api-tf-lock"
    encrypt        = true
  }
}