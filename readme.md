# gritum

**gritum** is the primary backend service for the **TRID Precheck** project.

It is a Clojure-based backend designed to be practical, deployable by a single developer, and suitable as a foundation for a commercial fintech-oriented service. The focus is on correctness, clarity, and operational simplicity rather than premature scale or abstraction.

This repository represents the **core backend** that other services, APIs, and infrastructure will build upon.

---

## Purpose

The initial goal of gritum is to power **TRID Precheck**, a service that helps validate and pre-check TRID-related data and workflows.

More broadly, gritum is intended to:

- Serve as a stable, long-lived backend foundation
- Favor explicit data models and predictable behavior
- Support gradual evolution into a larger platform if the product grows
- Remain operable and understandable by a single developer

---

## Non-Goals (for now)

- Building a generic framework
- Supporting multiple databases or cloud providers
- Over-optimizing for extreme scale
- Providing a polished open-source experience

This repository is product-driven first.

---

## Tech Stack (Current / Planned)

- **Language**: Clojure
- **Runtime**: JVM
- **API Style**: REST (GraphQL may be considered later)
- **Database**: PostgreSQL
- **Data Access**: SQL-first approach (e.g. HugSQL / HoneySQL style)
- **Auth**: JWT-based authentication
- **Deployment**: Docker-based, cloud-agnostic (initially simple VPS / managed container)
- **Environment**: Designed to run with minimal infrastructure dependencies

Specific libraries and versions may change as the project evolves.

---

## Design Principles

- **Explicit over implicit**
- **Data-oriented design**
- **Simple things first**
- **Operational realism**
- **Readable over clever**

The codebase is expected to reflect idiomatic Clojure, but not at the cost of approachability or debuggability.

---

## Project Structure (Tentative)

```text
gritum/
├── src/
│ └── gritum/
│ ├── api/ ; HTTP handlers, routing
│ ├── domain/ ; core domain logic
│ ├── db/ ; SQL, migrations, queries
│ ├── auth/ ; authentication & authorization
│ └── system.clj ; system wiring / lifecycle
├── resources/
│ └── migrations/
├── test/
├── Dockerfile
├── deps.edn
└── README.md
```

This structure is expected to evolve as requirements become clearer.

---

## Current Status

- Early stage
- APIs and schemas are still in flux
- Naming and boundaries are intentionally conservative
- Expect breaking changes

This repository should not yet be considered stable.

---

## Development Philosophy

This project is developed under the assumption that:

- Cloud resources cost real money
- Operational complexity compounds quickly
- One person should be able to understand the entire system
- Good defaults beat flexible abstractions

Decisions are biased toward what can realistically be built, deployed, and maintained alone.

---

## Local Development & Environment

This project follows the **12-Factor App** methodology for configuration. We use `direnv` to manage environment variables across different stages (local, production).

### 1. Prerequisites

- **direnv**: [Install direnv](https://direnv.net/docs/installation.html) and hook it into your shell.
- **Babashka**: Required for task orchestration via `control.clj`.

### 2. Environment Files

The project uses a "switching" strategy. Create these files in the root directory (already ignored by Git):

- `.env.local`: Variables for local development (e.g., local Postgres, dev LLM keys).
- `.env.prod`: Variables for production/cloud environment (GCP project IDs, Cloud SQL credentials).

### 3. Setup Flow

1. **Initialize the switch**: Create a `.envrc` file:

   # .envrc
   source_env .env.local
   # source_env .env.prod (Uncomment this when deploying)

2. **Authorize**: Run `direnv allow` in your terminal.

3. **Launch Editor**: Always launch your editor (Emacs, VS Code, etc.) from this terminal session to ensure it inherits the environment:

   emacs &  # or 'code .'

### 4. Security & Safety

- **Confidentiality**: Never commit `.envrc` or `.env.*` files. They contain production secrets.
- **Verification**: Check active variables anytime with `echo $GRITUM_ENV`.
- **Fail-Fast**: The application strictly validates required variables. If a variable is missing, the system will throw a 🚨 CRITICAL CONFIG ERROR and halt immediately.

### 5. Common Tasks

Tasks are managed via Babashka. Run `bb control.clj` to see available commands:

- `bb control.clj check`: Print currently loaded configuration.
- `bb control.clj migrate`: Run database migrations (uses proxy for prod).
- `bb control.clj thru`: Full deployment pipeline (migrate -> build -> press -> register -> deploy).

---
## License

License to be decided.

For now, assume this is **not** an open-source commitment.

---

## Notes

- The repository name **gritum** is intentionally abstract and not tied to the product name.
- The product-facing name is **TRID Precheck**.
- Branding, documentation, and external APIs will live elsewhere as the system matures.

---



