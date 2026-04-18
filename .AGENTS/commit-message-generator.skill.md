# Skill: Commit Message Generator

## When to Use This Pattern

- Every git commit in the vmetrix-query-manager repository
- When completing a Jira ticket or subtask via the jira-workflow skill

> **Golden Rule:** Every commit message must follow Conventional Commits. No exceptions.

## Repo Alignment

This skill follows the repo-level guidance in `CLAUDE.md`.

- Read `CLAUDE.md` first — it defines architecture, strict rules, and naming conventions.
- Commit messages must reference the Jira ticket key when working from a ticket.
- Keep commits scoped to one task or one subtask whenever practical.

If this skill conflicts with `CLAUDE.md`, follow `CLAUDE.md`.

---

## Format

```text
type(scope): description
```

- **type** — lowercase, from the allowed list (required)
- **scope** — module or area affected, in parentheses (recommended)
- **description** — imperative mood, lowercase start, no period at end (required)
- **max length** — 72 characters for subject line
- **body** — optional, max 100 characters per line

---

## Allowed Types

| Type       | When to Use                      | Example                                                  |
|------------|----------------------------------|----------------------------------------------------------|
| `feat`     | New feature for the user         | `feat(engine): add BFS join resolver`                    |
| `fix`      | Bug fix                          | `fix(filter): correct BETWEEN parameter binding`         |
| `docs`     | Documentation only               | `docs(api): update OpenAPI annotations`                  |
| `style`    | Formatting, no logic change      | `style(builder): fix indentation in QueryAssembler`      |
| `refactor` | Code restructure, no new feature | `refactor(metadata): extract field lookup method`        |
| `test`     | Adding or fixing tests           | `test(filter): add EqualStrategy unit tests`             |
| `chore`    | Build, config, tooling           | `chore(maven): update spring-boot-starter version`       |
| `perf`     | Performance improvement          | `perf(engine): cache metadata lookups`                   |
| `ci`       | CI/CD pipeline changes           | `ci(github): add maven test workflow`                    |
| `revert`   | Reverting a previous commit      | `revert(feat): remove broken comparator`                 |

---

## Scope Reference (vmetrix-query-manager)

| Scope          | Area                                                        |
|----------------|-------------------------------------------------------------|
| `engine`       | SQL generation engine (`domain/engine/`)                    |
| `filter`       | Filter tree & comparator strategies (`domain/engine/filter/`) |
| `join`         | Join resolution (`domain/engine/join/`)                     |
| `builder`      | SQL clause builders (`domain/engine/builder/`)              |
| `model`        | Domain models (`domain/model/`)                             |
| `metadata`     | Metadata service & repository (`application/`, `infrastructure/metadata/`) |
| `api`          | Controllers & DTOs (`api/`)                                 |
| `persistence`  | JPA entities & Spring Data repos (`infrastructure/persistence/`) |
| `shared`       | Cross-cutting concerns (`shared/`)                          |
| `maven`        | Maven build config (`pom.xml`)                              |
| `docs`         | Documentation (`README.md`, `CLAUDE.md`, OpenAPI)           |
| `db`           | Schema & data scripts (`schema.sql`, `data.sql`)           |

---

## Code Template: Writing a Commit Message

### Step 1 — Identify the type

Ask: "What did this change do?"

- Added something new → `feat`
- Fixed a bug → `fix`
- Changed docs → `docs`
- Restructured code → `refactor`
- Added/fixed tests → `test`
- Config/tooling/build → `chore`
- Improved performance → `perf`
- CI/CD change → `ci`

### Step 2 — Identify the scope

Ask: "Which module/layer was affected?"

- Use the scope reference table above.
- Map to the hexagonal architecture layer from `CLAUDE.md`.
- If multiple areas, use the most relevant one.
- If truly cross-cutting, scope can be omitted (rare).

### Step 3 — Write the description

- Start with imperative verb: `add`, `fix`, `remove`, `update`, `configure`, `extract`
- Lowercase first letter
- No period at the end
- Keep under 72 characters total

### Step 4 — Optional body

```text
type(scope): short description

Longer explanation if needed. Wrap at 100 characters.
Explain WHY, not WHAT (the diff shows what).
```

---

## Example Usage in vmetrix-query-manager Context

### Domain layer changes

```bash
git commit -m "feat(filter): add IsNotNull comparator strategy"
git commit -m "fix(join): correct BFS traversal for circular relationships"
git commit -m "test(builder): add QueryAssembler integration tests"
git commit -m "refactor(engine): extract parameter counter to shared utility"
```

### Metadata & infrastructure changes

```bash
git commit -m "feat(db): add META_RELATIONSHIP entries for issuer entity"
git commit -m "fix(metadata): handle unknown entity alias gracefully"
git commit -m "refactor(persistence): rename MetaTableEntity fields to match domain"
```

### API layer changes

```bash
git commit -m "feat(api): add POST /api/query/validate endpoint"
git commit -m "fix(api): return 400 for missing select fields in QueryRequest"
git commit -m "docs(api): add Springdoc annotations to MetadataController"
```

### With Jira ticket reference

```bash
git commit -m "feat(filter): SCRUM-100 add IsNotNull comparator strategy"
git commit -m "fix(join): SCRUM-105 correct BFS traversal for circular paths"
```

---

## Common Mistakes to Avoid

| ❌ Wrong | ✅ Correct | Rule Violated |
|----------|-----------|---------------|
| `fixed stuff` | `fix(filter): correct null check in EqualStrategy` | Missing type and scope |
| `Fix(Filter): Correct Null` | `fix(filter): correct null check` | Type must be lowercase |
| `feat(engine): add resolver.` | `feat(engine): add resolver` | No period at end |
| `WIP` | `chore(engine): scaffold join resolver` | Not a valid type |
| `feat: everything` | `feat(filter): add LIKE comparator strategy` | Too vague, missing scope |
| `FEAT(ENGINE): ADD RESOLVER` | `feat(engine): add resolver` | All lowercase required |
| `feat(engine): Added resolver` | `feat(engine): add resolver` | Use imperative mood |
| Subject line 90+ chars long | Keep under 72 characters | subject-max-length rule |

---

## Testing a Commit Message

```bash
# Verify the message matches the pattern: type(scope): description
echo "feat(filter): add IsNotNull strategy" | grep -P "^(feat|fix|docs|style|refactor|test|chore|perf|ci|revert)\(.+\): .+"

# Check recent history compliance (every line should match the pattern)
git log --oneline -10
```
