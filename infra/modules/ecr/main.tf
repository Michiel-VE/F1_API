resource "aws_ecr_repository" "f1_api" {
  name                 = "f1-api"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_lifecycle_policy" "f1_api_cleanup" {
  repository = aws_ecr_repository.f1_api.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Expire untagged images after 1 day"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 1
        }
        action = { type = "expire" }
      },
      {
        rulePriority = 2
        description  = "Keep last 3 tagged images"
        selection = {
          tagStatus      = "tagged"
          tagPatternList = ["v*"]
          countType      = "imageCountMoreThan"
          countNumber    = 3
        }
        action = { type = "expire" }
      }
    ]
  })
}

output "repository_arn" {
  value = aws_ecr_repository.f1_api.arn
}