# Contributing to MetricHive

Thank you for contributing to MetricHive! This guide details the development standards, code quality checks, and submission process.

---

## 1. Local Development Setup

### Prerequisites
* **Java 21 LTS** (`openjdk-21` or `temurin-21`)
* **Maven 3.9+** (or use included `./mvnw`)
* **Docker & Docker Compose**
* **Kind (Kubernetes in Docker)** & **Helm 3/4**
* **Terraform 1.5+**

### Building and Testing
```bash
# Compile and run test suite
mvn clean test

# Run Checkstyle audit
mvn checkstyle:check

# Package executable JAR
mvn clean package -DskipTests
```

---

## 2. Code Quality & Standards

* **Style Guide**: We enforce the **Google Java Style** via `checkstyle.xml`.
* **Zero Violations**: All pull requests must achieve `0 Checkstyle violations`. You can check compliance locally with:
  ```bash
  mvn checkstyle:check
  ```
* **Security & Vulnerability Gates**:
  * Avoid introducing vulnerable dependencies.
  * Secrets must **never** be checked into git. Use environment variables or Kubernetes Secrets.

---

## 3. Git Commit Guidelines

We adhere to the [Conventional Commits](https://www.conventionalcommits.org/) specification:

* `feat(...)`: New feature or endpoint
* `fix(...)`: Bug fix
* `infra(...)`: Terraform or cloud infrastructure changes
* `k8s(...)`: Kubernetes manifests or helm configuration
* `ci(...)`: GitHub Actions workflow changes
* `docs(...)`: Documentation or runbook updates
* `refactor(...)`: Code refactoring with no functional changes

---

## 4. Pull Request Process

1. Fork or branch off `main` with a descriptive branch name (e.g. `feat/new-query-filter`).
2. Implement changes, write corresponding unit/integration tests, and ensure `mvn test` passes.
3. Validate Checkstyle: `mvn checkstyle:check`.
4. Open a Pull Request against `main`. Fill out the [Pull Request Template](.github/pull_request_template.md).
5. All 5 pre-merge CI checks (Lint, Test, Security FS, Build, Security Image) must turn green before merging.
