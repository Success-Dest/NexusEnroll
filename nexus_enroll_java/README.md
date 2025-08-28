# NexusEnroll - Java Proof of Concept

This repository is a compact Java PoC for the NexusEnroll assignment (UCSC SCS 2303).
It focuses on the **business tier** and demonstrates the following design patterns:

- Observer (Waitlist -> Notification)
- Chain of Responsibility (Enrollment validators)
- Factory Method (User creation)
- Facade / Coordinator (EnrollmentService)

## How to run
Requirements: Java 11+ and `javac` / `java` on PATH.

From the repository root run (Unix-like shell):

```bash
cd src
javac nexus/**/*.java
java nexus.Main
```

Or compile with an IDE (IntelliJ/Eclipse) by importing `src` as the source folder.

## What the demo does
- Creates two students (Alice, Bob) and a course `CS102` with prerequisite `CS101`.
- Section `SEC1` has capacity 1.
- Alice (no prereq) attempts to enroll -> placed on waitlist.
- Bob (has prereq) enrolls successfully (occupies the seat).
- Alice drops -> WaitlistManager promotes next student (Alice) and NotificationService prints alerts.
- A small grade submission batch is processed showing graceful handling of invalid grades.

## Files of interest
- `src/nexus/service/EnrollmentService.java` — core facade/coordinator for enroll/drop flows.
- `src/nexus/validators/*` — Validator implementations used in the chain.
- `src/nexus/waitlist/WaitlistManager.java` and `src/nexus/notification/NotificationService.java` — Observer pattern.
- `src/nexus/Main.java` — Entry point showcasing demo flows.

## Notes
This PoC uses in-memory repositories for simplicity. For the actual assignment you can extend
repositories to use JDBC/SQLite or a proper ORM, and add transactional DB handling and REST endpoints.
