# F1 API

## Overview

The F1 API serves as the backend for the Formula 1 Dashboard project. It handles all data processing, including scraping, storing, and providing access to Formula 1 data via REST endpoints. The API is built with Java 17 and Spring Boot, and is deployed as an AWS Lambda function behind an API Gateway.

This API powers the frontend application, which interacts with the backend to display Formula 1 data such as standings, teams, and races.

## Architecture

- **Backend**: Java 17, Spring Boot
- **Authentication**: JWT, Bearer Tokens, and Google OAuth
- **Database**: PostgreSQL (Required)
- **Scraping**: Jsoup (Scheduled with Cron)
- **Deployment**: AWS Lambda with AWS API Gateway

## Data Flow

1. Data is scraped from the official [**f1.com**](www.f1.com) website using Jsoup.
2. The scraped data is stored in a PostgreSQL database.
3. The API provides endpoints for retrieving the data, including historical standings, teams, and races.
4. All API calls are authenticated using JWT tokens or Google OAuth.

## Authentication

This API uses JWT and Bearer Tokens for authentication. Google OAuth is also supported for user authentication.

The API expects a valid token to be included in the `Authorization` header for all requests that require authentication.

## Environment Configuration

The API supports two environments:

- **Local**: Configuration values are stored in the `.env` file, which is loaded into `application.properties`.
- **Production**: Configuration values, including secrets, are stored in AWS Secrets Manager.

**Local Environment Setup**:  
Ensure the `.env` file contains all necessary configuration variables, such as database connection strings and JWT secrets.

**Production Environment**:  
AWS Secrets Manager is used to securely store and access configuration values for the production environment.

## Endpoints

All endpoints are secured and require authentication via JWT or Google OAuth. 

While Swagger is available locally for development purposes, it is not included in the production environment.

### Example endpoints:

- `GET /api/drivers`: Retrieve current Formula 1 standings.
- `GET /api/team`: Retrieve list of teams in the current season.
- `GET /api/races`: Retrieve information on upcoming and past races.
