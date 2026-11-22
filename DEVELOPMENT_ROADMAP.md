# MandiConnect Development Roadmap

## Project assessment

MandiConnect is already a Java Swing desktop application built with Maven. Its
existing layout separates the UI, services, data access, models, database
connection, and hand-written data structures under
`MandiConnect/MandiConnectSwing/src`. The project also includes a MySQL schema
and JUnit tests for its six data structures.

This roadmap is an incremental plan for understanding and validating the
existing application before extending it; it does not assume the project is an
empty scaffold.

## Stages

| Stage | Scope | Status |
| --- | --- | --- |
| 1. Project baseline and setup | Record the existing architecture, add ignore rules for local IDE/build output, and establish the Maven test baseline. | In progress |
| 2. Data-structure core | Review the six custom structures and their focused unit tests; fix only demonstrated correctness issues. | Pending |
| 3. Data and persistence | Review models, DAO boundaries, database configuration, and schema setup; validate persistence behavior. | Pending |
| 4. Business workflows | Trace and test user registration, lot management, bidding, and auction state transitions in the service layer. | Pending |
| 5. Desktop application | Review and improve the Swing login, farmer, trader, admin, and algorithm-console flows. | Pending |
| 6. End-to-end readiness | Run the available automated checks, verify packaging and startup instructions, and document remaining operational requirements. | Pending |

## Stage 1: project baseline and setup

### Existing foundation

- Java 22 is the configured compilation target in `pom.xml`.
- Maven Wrapper scripts are present; the Windows wrapper uses the bundled
  Maven 3.9.9 when available and otherwise falls back to `mvn`.
- Application code is organized under `com.mandiconnect` by responsibility:
  `ui`, `service`, `dao`, `model`, `db`, and `dsa`.
- The database schema is in `MandiConnect/MandiConnectSwing/database`.
- The existing project README documents database setup and application usage.

### Local verification

The local environment has Java 25 and Maven 3.9.9. The requested baseline
command is:

```powershell
.\MandiConnect\MandiConnectSwing\mvnw.cmd -f .\MandiConnect\MandiConnectSwing\pom.xml clean test
```

This command was not run in this stage because permission to execute it was
declined. The automated test baseline therefore remains unverified.

### Completion criteria

- [x] Record the existing architecture and staged development plan.
- [x] Ignore generated Maven output and local IDE metadata.
- [ ] Run the clean Maven test suite and record its result.
