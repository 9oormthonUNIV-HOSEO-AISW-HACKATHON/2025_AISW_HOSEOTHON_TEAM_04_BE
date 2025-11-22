# Repository Guidelines

## Project Structure & Module Organization
- `backend/`: Spring Boot API (Java 17, Gradle). Domain code under `backend/src/main/java/com/example/familyq/{domain,global,support}`, configuration in `backend/src/main/resources/application.yml`. Tests sit in `backend/src/test/java` with shared fixtures in `support`.
- `frontend/`: React 19 + Vite. UI code in `frontend/src/{pages,components,api,contexts,styles,utils}`, build artifacts in `frontend/dist`, and `frontend/Dockerfile` + `nginx.conf` for containerized hosting.
- `docs/`: specification and schema references; `scripts/`: helper scripts such as `test-db-connection.sh`; `FamilyQ_Postman_Collection.json`: request examples for API verification.

## Build, Test, and Development Commands
- Backend dev server: `cd backend && ./gradlew bootRun` (defaults to `dev` profile with in-memory H2). Switch to MariaDB with `SPRING_PROFILES_ACTIVE=prod` and DB env vars.
- Backend tests: `cd backend && ./gradlew test` (JUnit 5, `@ActiveProfiles("dev")`). Full bundle: `./gradlew build`. DB connectivity check: `DB_URL=... ./scripts/test-db-connection.sh` which runs the `testDbConnection` Gradle task.
- Frontend: `cd frontend && npm install` once, then `npm run dev` for local preview, `npm run build` for production assets, `npm run preview` to serve the built bundle.

## Coding Style & Naming Conventions
- Java: 4-space indent, package prefix `com.example.familyq`. Suffix services/repositories/controllers accordingly; DTOs end with `Request`/`Response`. Lombok is available—prefer it for boilerplate, but keep explicit code when clarity helps.
- JavaScript/JSX: functional components with hooks; PascalCase for components, camelCase for utilities. Keep files co-located with their feature folder in `src`.
- Favor concise REST paths and clear logging; avoid committing secrets—use environment variables to override defaults in `application.yml`.

## Testing Guidelines
- Unit/integration tests live next to matching packages under `backend/src/test/java`; class names end with `*Test`. Integration tests often extend `IntegrationTestSupport` (activates `dev` profile, transactional).
- Use AssertJ for fluent assertions. Prefer H2-backed tests; only hit MariaDB when necessary via the `prod` profile or `testDbConnection`.
- Frontend has no test harness yet; add Vitest/RTL when introducing UI logic that benefits from coverage and document commands alongside new tests.

## Commit & Pull Request Guidelines
- Commit messages are short and descriptive (English or Korean acceptable); prefer imperative scope-first phrasing such as `feat: add family join flow` or `fix: adjust question service`.
- For PRs: include a brief summary, linked issue or ticket, testing notes (`./gradlew test`, `npm run build`, etc.), and screenshots/gifs for UI changes. Call out database/schema changes and required env vars. Keep diffs focused and avoid mixing refactors with feature work.
