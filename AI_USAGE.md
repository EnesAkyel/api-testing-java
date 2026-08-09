# AI Usage Log

> This document records how Claude (Anthropic) was used as an AI pair-programmer throughout this project's development cycle.

## Overview

Java REST Assured API testing suite targeting `movie-catalog-api`. Mirrors the TypeScript suite in coverage but demonstrates the Java/REST Assured idiom for hiring managers who evaluate Java automation skills.

## Planning & Architecture

- Designed the layered package structure: `api/` (request builders), `base/` (BaseTest), `model/` (POJOs), `config/` — separated from test classes in `smoke/`, `contract/`, `integration/`, `regression/`
- Chose REST Assured's fluent DSL with a shared `RequestSpecification` base spec over raw HTTP clients
- Used `ConfigManager` to load `BASE_URL` from environment, keeping tests environment-agnostic

## Code & Implementation

- Implemented `MoviesApi` and `StudiosApi` request builders following the page-object equivalent pattern for API tests
- Wrote contract tests with inline JSON schema assertions using REST Assured's `matchesJsonSchemaInClasspath`
- Structured smoke, integration, contract, and regression suites as independent test classes

## CI/CD & Infrastructure

- Integrated into `movie-catalog-api` CI as a parallel job (uses the same `start-api` composite action)
- Allure results uploaded as CI artifact for report review