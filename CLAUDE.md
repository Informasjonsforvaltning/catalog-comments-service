# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Catalog Comments Service is a Kotlin/Spring Boot 4 microservice that provides a REST API for managing comments on datasets, dataservices, concepts, and services. It uses MongoDB for persistence and OAuth2/JWT for authentication.

## Build and Development Commands

```bash
# Build and package
mvn clean package

# Run locally (requires MongoDB via docker compose)
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=develop

# Run all tests with coverage
mvn verify

# Run only unit tests
mvn test

# Run only integration tests
mvn failsafe:integration-test

# Run a single test class
mvn test -Dtest=CommentServiceTest

# Run a single test method
mvn test -Dtest=CommentServiceTest#testMethodName
```

## Architecture

**Layered structure:**
- `controller/` - REST endpoints with `@PreAuthorize` security annotations
- `service/` - Business logic and DTO/DBO mapping
- `repository/` - Spring Data MongoDB DAOs
- `model/` - Separate DTO (API) and DBO (database) representations
- `security/` - OAuth2 JWT configuration and custom `Authorizer` for role-based access

**API endpoints** follow pattern `/{orgNumber}/{topicId}/comment` with CRUD operations.

**Authorization roles:**
- `system:root:admin` - Full system access
- `organization:{orgNumber}:{admin|write|read}` - Organization-scoped access

## Testing

Tests are organized by type and tagged:
- `src/test/kotlin/.../unit/` - Unit tests (`@Tag("unit")`) with mocked dependencies
- `src/test/kotlin/.../integration/` - Integration tests (`@Tag("integration")`) using TestContainers for MongoDB

Key test utilities:
- `ApiTestContext` - Base class for integration tests, manages TestContainers and mock OIDC server
- `TestData.kt` - Test fixtures and constants
- `JwtUtils.kt` - JWT token generation for OAuth2 testing

## Configuration

Spring profiles in `application.yml`:
- `develop` - Local development (localhost MongoDB, wildcard CORS)
- `integration-test` - CI testing with TestContainers and mock OIDC at localhost:5050

Environment variables: `MONGODB_HOST`, `MONGODB_USER`, `MONGODB_PASSWORD`, `OIDC_JWKS`, `OIDC_ISSUER`, `CORS_ORIGIN_PATTERNS`

## Tech Stack

- Java 21, Kotlin 2.2, Spring Boot 4.0
- MongoDB with Spring Data
- OAuth2 Resource Server with Nimbus JOSE+JWT
- JUnit 5, Mockito-Kotlin, WireMock, TestContainers
- SpringDoc OpenAPI (Swagger UI at `/swagger-ui/index.html`)
