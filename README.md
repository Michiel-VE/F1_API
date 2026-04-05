# F1 API

## Overview

The F1 API serves as the backend for the Formula 1 Dashboard project. It handles all data processing, including scraping, storing, and providing access to Formula 1 data via REST endpoints. 

The API is built with **Java 17** and **Spring Boot 3**, containerized with **Docker**, and deployed as **AWS Lambda** functions behind an **API Gateway**.

## Architecture

- **Language**: Java 21
- **Framework**: Spring Boot 3
- **Infrastructure**: Terraform (IaC)
- **Containerization**: Amazon ECR (Elastic Container Registry)
- **Compute**: AWS Lambda (Image Packaging Type)
- **API Management**: AWS API Gateway (REST)
- **Database**: PostgreSQL
- **Security**: JWT, Bearer Tokens, and Google OAuth 2.0
- **Scraping**: Jsoup (Scheduled via EventBridge/CloudWatch Crons)

## Data Flow

1. **Scraping**: Scheduled Lambda handlers (Race, Driver, Standing) scrape data from [f1.com](https://www.f1.com) using Jsoup.
2. **Storage**: Scraped data is persisted in a PostgreSQL database; schema is managed via Flyway migrations.
3. **Consumption**: The main API Lambda serves REST endpoints to the frontend.
4. **Security**: Requests are validated via JWT tokens or authenticated through Google OAuth.

## Authentication

This API uses JWT and Bearer Tokens for authentication. Google OAuth is also supported for user authentication. The API expects a valid token to be included in the `Authorization` header for all requests that require authentication.

## Environment Configuration

- **Local**: Configuration is managed via a `.env` file loaded into `application.properties`.
- **Production**: Infrastructure and secrets are managed via **Terraform** and **AWS Secrets Manager**.

**Local Environment Setup**:  
Ensure the `.env` file contains all necessary configuration variables, such as database connection strings and JWT secrets.

## Running Instructions

### Database Migrations
This project uses Flyway for database versioning and schema management.
- **Location**: Migration scripts are located in `src/main/resources/db/migration`.
- **Execution**: Migrations run automatically when the application starts.
- **Naming Convention**: New migrations must follow the pattern `V<Number>__<Description>.sql` (e.g., `V2__create_tables.sql`). Note the double underscore.

### Local Development
To run the Spring Boot application locally:
```powershell
./gradlew bootRun
```

Build for Local Testing
To create a standard Spring Boot JAR (for local testing only):

```PowerShell
./gradlew bootJar
# Output: build/libs/f1_api-local.jar
```

### Deployment Process
The project is deployed using a PowerShell automation script (deploy.ps1) which performs the following:

- **Build**: Generates a "Shadow JAR" to ensure the correct structure for Lambda.

- **Containerize**: Builds a Docker image using the public.ecr.aws/lambda/java:21 base.

- **Registry**: Authenticates with AWS ECR and pushes the tagged image.

- **Cleanup**: Removes the local Docker image after a successful push to save disk space.

- **Infrastructure**: Runs terraform apply to update the Lambda functions with the new image URI.

To deploy to production:

```PowerShell
./deploy.ps1
```

## Endpoints
All endpoints are secured and require authentication via JWT or Google OAuth.

While Swagger is available locally for development purposes, it is not included in the production environment.

Example endpoints:
- GET /api/v1/drivers: Retrieve current Formula 1 standings.

- GET /api/v1/team: Retrieve list of teams in the current season.

- GET /api/v1/races: Retrieve information on upcoming and past races.