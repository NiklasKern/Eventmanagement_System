# Eventmanagement_System
Repository for our Outdoor Evenentmanagement System.
---
### `main` (Production Branch)
* **Purpose:** Contains stable, production-ready code.
* * **Usage:** Code in `main` should always be fully tested and deployable. Direct commits are strictly prohibited. Code only enters `main` via Pull Requests merged from `dev`.
---
### `dev` (Development Branch)
* **Purpose:** Serves as the central integration branch for active development.
* **Usage:** All completed features are to be merged to `dev`. It contains the latest code targeted for the next release. Allways base new feature branches of `dev`.
---
### `feature/*` (Feature Branches)
* **Purpose:** Isolated enviroment for developing specific features/user stories.
* **Naming Convention:** `feature/feature-name` (e.g. `feature/add-event`.
* **Usage:** Branch of `dev`: `git checkout dev && git checkout -b feature/feature-name`. Implement and commit features/task. Once all subtasks are done and quality assurence is done, push the branch to Github and open a **Pull Request into `dev`**.
