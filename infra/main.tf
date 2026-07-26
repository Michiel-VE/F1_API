locals {
  image_tag = var.image_tag
  ecr_base  = "${var.aws_account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
  image_uri = "${local.ecr_base}/f1-api:${local.image_tag}"
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
  db_secret_arn      = module.secrets.db_secret_arn
  google_secret_arn  = module.secrets.google_secret_arn
  upstash_secret_arn = module.secrets.upstash_secret_arn
}

module "secrets" {
  source                    = "./modules/secrets"
  db_secret_name_prefix     = var.db_secret_name_prefix
  google_secret_name_prefix = var.google_secret_name_prefix
  upstash_secret_name_prefix = var.upstash_secret_name_prefix
  db_username               = var.db_username
  db_password               = var.db_password
  db_url                    = var.db_url
  google_client             = var.google_client
  google_client_secret      = var.google_client_secret
  jwt_secret_key            = var.jwt_secret_key
  upstash_redis_token       = var.upstash_redis_token
  upstash_redis_url         = var.upstash_redis_url
}

module "lambdas" {
  source              = "./modules/lambdas"
  image_uri           = local.image_uri
  lambda_role_arn     = module.iam.lambda_role_arn
  env                 = var.env
  db_secret_name      = module.secrets.db_secret_name
  google_secret_name  = module.secrets.google_secret_name
  upstash_secret_name = module.secrets.upstash_secret_name
  jwt_secret_key      = var.jwt_secret_key
}

module "api_gateway" {
  source                  = "./modules/api_gateway"
  lambda_alias_arn        = module.lambdas.lambda_alias_arn
  lambda_alias_invoke_arn = module.lambdas.lambda_alias_invoke_arn
}