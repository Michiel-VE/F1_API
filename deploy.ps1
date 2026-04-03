Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# --- Config ---
$region    = "eu-north-1"
$accountId = "633620335077"
$imageTag  = "v$(Get-Date -Format 'yyyyMMddHHmmss')"
$registry  = "$accountId.dkr.ecr.$region.amazonaws.com"
$fullUri   = "$registry/f1-api:$imageTag"

Write-Host "==> Building JAR..." -ForegroundColor Cyan
& ./gradlew.bat build -x test
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed" }

Write-Host "==> Logging into ECR..." -ForegroundColor Cyan
$token = aws ecr get-login-password --region $region
docker login --username AWS --password $token $registry
if ($LASTEXITCODE -ne 0) { throw "ECR login failed" }

Write-Host "==> Building Docker image: $fullUri" -ForegroundColor Cyan
docker build --platform linux/amd64 --provenance=false -t $fullUri .
if ($LASTEXITCODE -ne 0) { throw "Docker build failed" }

Write-Host "==> Pushing image to ECR..." -ForegroundColor Cyan
docker push $fullUri
if ($LASTEXITCODE -ne 0) { throw "Docker push failed" }

Write-Host "==> Image pushed: $fullUri" -ForegroundColor Green

Write-Host "==> Cleaning up local image..." -ForegroundColor Gray
docker rmi $fullUri
docker image prune -f --filter "label=stage=builder" 

Write-Host "==> Running Terraform..." -ForegroundColor Cyan

$env:TF_VAR_image_tag = $imageTag
Set-Location infra
terraform apply -auto-approve
Set-Location ..

Write-Host "==> Deploy complete!" -ForegroundColor Green