# Eventmanagement_System
Repository for our Outdoor Eventmanagement System.

---
## Local Setup

### Prerequisites
The local PostgreSQL database runs in a Docker container that is started automatically by Spring Boot (Docker Compose support).

* **Windows:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) must be installed and **running in the background** before starting the application.
* **Linux:** Docker and the Docker Compose plugin (`docker compose`) must be installed and the Docker service must be running.

### Database Access (IntelliJ)
The database can be accessed in IntelliJ via the **Database** tool window (database icon on the right side). Add a new PostgreSQL data source with the following credentials:

| Setting  | Value       |
|----------|-------------|
| Host     | `localhost` |
| Port     | `5432`      |
| Database | `ems`       |
| User     | `dev`       |
| Password | `fhv2026`   |

> These credentials are for local development only. The application must have been started at least once so the container is running.

---
## Branching Strategy

### `main` (Production Branch)
* **Purpose:** Contains stable, production-ready code.
* **Usage:** Code in `main` should always be fully tested and deployable. Direct commits are strictly prohibited. Code only enters `main` via Pull Requests merged from `dev`.

---
### `dev` (Development Branch)
* **Purpose:** Serves as the central integration branch for active development.
* **Usage:** All completed features are to be merged into `dev`. It contains the latest code targeted for the next release. Always base new feature branches off `dev`.

---
### `feature/*` (Feature Branches)
* **Purpose:** Isolated environment for developing specific features/user stories.
* **Naming Convention:** `feature/feature-name` (e.g. `feature/add-event`).
* **Usage:** Branch off `dev`: `git checkout dev && git checkout -b feature/feature-name`. Implement and commit features/tasks. Once all subtasks and quality assurance are done, push the branch to GitHub and open a **Pull Request into `dev`**.