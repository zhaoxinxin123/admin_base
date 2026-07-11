# CLAUDE.md

This repository uses `AGENTS.md` as the canonical development guide for coding agents.

Before making changes, read:

- `AGENTS.md` for project architecture, testing rules, auth/persistence conventions and module extension workflow.
- `README.md` for the user-facing project overview and how to add new modules.

Important current-state reminders:

- The project is already on Spring Boot 3.5.x and Java 17.
- The primary persistence path is Spring Data JPA, not new MyBatis code.
- The current auth architecture supports both local JWT mode and OAuth2 Resource Server mode.
- Tests use the `test` profile, which is configured for MySQL/Redis on `192.168.3.3`; do not start local MySQL/Redis unless explicitly requested.
- Run focused tests for the changed area and `mvn test` before claiming completion.
