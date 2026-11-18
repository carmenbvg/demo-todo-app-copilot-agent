# Repository instructions

## Project overview

This repository is a Java 21 web application built with Spring Boot and Maven. Keep changes focused, easy to maintain, and consistent with Spring MVC conventions.

## Architecture

- Keep a clear separation between web, business, and data access concerns.
- Keep the application entry point in the root application package so component scanning covers the application.
- Organize production code by responsibility: web controllers, services, repositories, and domain/model types. Add layers only when they clarify responsibilities.
- Keep HTTP concerns in controllers, business logic in services, and persistence details in repositories. Use request/response DTOs at API boundaries when appropriate.
- Keep configuration externalized in `src/main/resources/application.properties` or environment-specific configuration; do not hard-code secrets.

## Coding and naming conventions

- Use Java 21 and standard Java formatting. Keep types small and cohesive, prefer immutable data where practical, and avoid unnecessary abstractions.
- Use lowercase package names; `UpperCamelCase` for classes, interfaces, records, and enums; `lowerCamelCase` for methods and fields; and `UPPER_SNAKE_CASE` for constants.
- Name types for their role (for example, `*Controller`, `*Service`, and `*Repository`) and tests after the type or behavior they cover.
- Prefer clear, descriptive names and explicit types where they improve readability. Avoid wildcard imports and unused code.

## Dependency injection

- Use Spring-managed components and constructor injection. Prefer a single constructor and `final` fields for required dependencies.
- Avoid field injection and avoid introducing a dependency-injection framework beyond Spring.

## Error handling

- Validate incoming data at application boundaries and return appropriate HTTP status codes.
- Handle expected failures explicitly. Use centralized exception handling (such as `@ControllerAdvice`) for consistent API error responses; do not expose stack traces or internal details to clients.
- Do not silently swallow exceptions. Log failures with useful context, without logging secrets or sensitive data.

## Testing

- Add or update tests for behavior changed. Cover important success cases, validation, and expected failure paths.
- Use focused unit tests for business logic and Spring MVC or application-context tests where framework integration needs verification.
- Keep tests deterministic and independent of external services unless an integration test specifically requires them.

## Maven

Use the Maven Wrapper from the repository root:

- `./mvnw test` — run tests
- `./mvnw verify` — run the full Maven verification lifecycle
- `./mvnw package` — build the packaged application
- `./mvnw spring-boot:run` — run the application locally

Run the relevant tests and build checks after changes. Do not add dependencies unless they are needed; use the existing Spring Boot and Maven setup where possible.
