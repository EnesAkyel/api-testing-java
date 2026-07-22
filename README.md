# api-testing-java

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![REST Assured](https://img.shields.io/badge/REST_Assured-6.0-4DB33D?style=for-the-badge)
![JUnit 6](https://img.shields.io/badge/JUnit_6-6.1-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Allure](https://img.shields.io/badge/Allure-2.35-orange?style=for-the-badge)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

Java counterpart to [api-testing-ts](https://github.com/EnesAkyel/api-testing-ts). Same test coverage, same target API, different language and tool stack. Demonstrates that the same quality bar — smoke, contract, integration, regression — can be implemented in both TypeScript/Jest and Java/REST Assured.

**Target:** [movie-catalog-api](https://github.com/EnesAkyel/movie-catalog-api) (Spring Boot · Postgres)

---

## Tech Stack

| Tool         | Version | Purpose                                     |
|--------------|---------|---------------------------------------------|
| Java         | 25      | Language                                    |
| REST Assured | 6.0.1   | HTTP client, schema validation, assertions  |
| JUnit        | 6.1.2   | Test runner, parameterized tests, lifecycle |
| Allure       | 2.35.3  | Test reporting                              |
| Jackson      | 3.2.1   | JSON deserialization                        |
| Lombok       | 1.18.46 | Model boilerplate                           |
| Owner        | 1.0.12  | Config/env management                       |
| Maven        | 3.9+    | Build and dependency management             |

---

## Project Structure

```
src/
├── main/java/com/apitest/
│   ├── config/
│   │   ├── AppConfig.java        # Owner @Config interface
│   │   └── ConfigManager.java    # Singleton accessor
│   └── model/
│       ├── Movie.java            # Response model
│       ├── Studio.java
│       ├── PageResponse.java     # Generic paginated response wrapper
│       └── MovieFilters.java     # Builder for optional query params
└── test/
    ├── java/com/apitest/
    │   ├── api/
    │   │   ├── MoviesApi.java    # REST Assured request builders for /movies
    │   │   └── StudiosApi.java   # REST Assured request builders for /studios
    │   ├── base/
    │   │   └── BaseTest.java     # Global setup: base URI, Allure filter, logging
    │   ├── smoke/
    │   │   ├── MoviesSmokeTest.java
    │   │   └── StudiosSmokeTest.java
    │   ├── contract/
    │   │   ├── MoviesContractTest.java
    │   │   └── StudiosContractTest.java
    │   ├── integration/
    │   │   ├── MoviesApiTest.java
    │   │   └── StudiosApiTest.java
    │   └── regression/
    │       └── MoviesRegressionTest.java
    └── resources/
        ├── config.properties
        ├── allure.properties
        └── schemas/
            ├── movie.json         # JSON Schema draft-07 for a single Movie
            ├── studio.json        # JSON Schema draft-07 for a single Studio
            ├── page-movies.json   # Paginated Movie response schema
            └── page-studios.json  # Paginated Studio response schema
```

---

## Test Suites

| Suite       | Tag                   | What it covers                                                                                                                                                         |
|-------------|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Smoke       | `@Tag("smoke")`       | GET /movies 200, GET /movie/1001 200, GET /studios 200                                                                                                                 |
| Contract    | `@Tag("contract")`    | JSON Schema validation on all response shapes, pagination fields, per-item field types                                                                                 |
| Integration | `@Tag("integration")` | Full CRUD for Movies and Studios, filter params, pagination, all negative paths (404, 409, 400 with field errors)                                                      |
| Regression  | `@Tag("regression")`  | Collection integrity (30 movies, unique MIDs, no orphaned studio refs), data consistency between single and list endpoints, write lifecycle (create → update → delete) |

### Comparison with api-testing-ts

Same endpoints, same test IDs, same assertion logic — different language and tooling:

| Concern                 | api-testing-ts                   | api-testing-java                     |
|-------------------------|----------------------------------|--------------------------------------|
| HTTP client             | Axios wrapper                    | REST Assured                         |
| Schema validation       | AJV + JSON Schema                | REST Assured `json-schema-validator` |
| Test runner             | Jest                             | JUnit 6                              |
| Response timing         | Custom `toRespondWithin` matcher | REST Assured `.time(lessThan(...))`  |
| Generic deserialization | TypeScript generics              | Jackson `TypeRef`                    |
| Test suites             | 4 Jest config files              | JUnit 6 `@Tag` + `-Dgroups`          |
| Report                  | Jest HTML + Allure               | Allure                               |

---

## Prerequisites

- Java 25+
- Maven 3.9+
- movie-catalog-api running on `http://localhost:8080/api/v1`

### Start movie-catalog-api

```bash
git clone https://github.com/EnesAkyel/movie-catalog-api
cd movie-catalog-api
docker compose up -d --build
```

Wait ~30s for Spring Boot + Postgres to initialize before running tests.

---

## Running Tests

```bash
# Full suite (all tags)
mvn test

# By suite
mvn test -Dgroups=smoke
mvn test -Dgroups=contract
mvn test -Dgroups=integration
mvn test -Dgroups=regression
```

---

## Allure Report

```bash
mvn test
mvn allure:report
open target/site/allure-maven-plugin/index.html
```

Or serve it live:

```bash
mvn allure:serve
```

---

## CI/CD

GitHub Actions runs on every push and pull request to `main`.

**`test` job** — starts movie-catalog-api via Docker Compose, polls until ready, then runs smoke → contract → integration in sequence. Uploads Allure report as a build artifact (30-day retention).

**`regression` job** — runs only on pushes to `main` (not on PRs). Same Docker Compose startup. Uploads a separate regression report (14-day retention).

If smoke fails (API unreachable), contract and integration are skipped. The failure is diagnosable without reading the full test output.

See [`.github/workflows/api-tests.yml`](.github/workflows/api-tests.yml).

---

## Author

**Enes Akyel**  
SDET | QA Automation Engineer  
[LinkedIn](https://www.linkedin.com/in/enes-akyel-2a77a7122/) · [GitHub](https://github.com/EnesAkyel)