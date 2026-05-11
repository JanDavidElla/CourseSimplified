# CourseSimplified

CourseSimplified is a Java/JavaFX degree-planning application for San Jose State University roadmaps. It loads course and program data from an external API, builds a prerequisite tree, and lets each user track course progress as `Remaining`, `In Progress`, or `Completed`.

## Overview

The current root project includes:

- A JavaFX GUI with login, registration, and per-user saved progress
- A roadmap viewer with prerequisite tree navigation
- Course status management with prerequisite validation
- Local JSON persistence for user accounts and roadmap progress
- A CLI entry point preserved from earlier development stages

## Features

- Sign in or create an account before entering the planner
- Select a major:
  - Computer Science
  - Software Engineering
- Load and browse the roadmap as a prerequisite tree
- Update course status:
  - `Remaining`
  - `In Progress`
  - `Completed`
- Prevent users from marking a course `In Progress` or `Completed` until prerequisites are completed
- Show progress counts for completed, in-progress, and remaining courses
- Restore user-specific progress and last selected major across sessions

## Tech Stack

- Java 21
- JavaFX 21.0.2
- Maven
- Gson
- JSoup
- JUnit

## Project Structure

```text
src/main/java/coursesimplified/
├── api/           External API access
├── cli/           Command-line interface
├── display/       Console tree rendering
├── gui/           JavaFX application, login, and planner UI
├── model/         Core domain model
├── repository/    Roadmap loading abstractions and implementations
├── service/       Application logic, persistence, authentication
├── CourseSimplifiedBootstrap.java
└── Main.java
```

## Architecture

CourseSimplified follows a layered design:

- `gui` handles the JavaFX interface
- `service` handles authentication, roadmap workflow, and status updates
- `repository` loads roadmap data
- `api` retrieves external course/program information
- `model` represents majors, courses, graphs, users, and statuses

The main Facade in the system is `CourseTreeService`, which simplifies roadmap loading, progress retrieval, and course status updates for both the GUI and CLI.

## Requirements

- Java 21
- Maven 3.x
- Internet connection for loading roadmap data from the external API

## Build

```bash
mvn clean compile
```

## Run

### JavaFX GUI

```bash
mvn javafx:run
```

The GUI is the primary interface for the project.

### CLI

```bash
mvn exec:java
```

## Test

```bash
mvn test
```

The automated tests cover roadmap loading, status updates, invalid course handling, progress counting, prerequisite enforcement, and JSON persistence behavior.

## Persistence

- User accounts are stored locally with hashed passwords
- Course progress is stored locally per user in JSON
- Legacy completed-only progress data remains compatible with the current persistence format

## Notes

- The app opens to the login screen first
- The planner UI is the main submission interface
