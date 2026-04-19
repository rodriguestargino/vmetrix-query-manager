# Skill: Jira Workflow

## When to Use This Pattern

- Picking up a new Jira ticket from the backlog
- Resolving a bug report or implementing a feature request
- Working parent tickets that include subtasks
- Keeping Jira comments, verification, and commits aligned with completed work

> **Golden Rule:** Do not write feature code before checking the Jira acceptance criteria and the current state of the affected files.

## Repo Alignment

This skill follows the repo-level guidance in `CLAUDE.md`.

- Read `CLAUDE.md` first — it defines architecture, strict rules, naming conventions, and design patterns.
- If the task touches the SQL engine, verify it against the **Active Design Patterns** and **Strict Rules** sections.
- If the task adds a new entity or field, confirm it is metadata-driven (zero Java code changes).
- Prefer the smallest change that satisfies the acceptance criteria.
- Leave unrelated dirty files untouched.

If this skill conflicts with `CLAUDE.md`, follow `CLAUDE.md`.

## Workflow Phases

### Phase 1: Ticket Acquisition

Use Jira MCP to fetch the ticket. Extract:

- Key and title
- Type (Story, Task, Bug, Subtask)
- Current status
- Acceptance criteria
- Subtasks (if parent ticket)
- Linked context that changes implementation scope

Convert the acceptance criteria into a checklist before editing.

### Phase 2: Scope and Codebase Verification

Inspect the affected area before making changes.

Recommended checks:

1. Identify the target layer in the hexagonal architecture (`api/`, `application/`, `domain/`, `infrastructure/`, `shared/`).
2. Inspect the current implementation and neighboring files.
3. Inspect relevant tests to understand patterns and coverage.
4. Verify the change respects **layer dependency rules** from `CLAUDE.md`.
5. Inspect metadata tables (`META_TABLE`, `META_COLUMN`, `META_RELATIONSHIP`) when the task involves entities or fields.

Command guidance for this repo:

- Use `rg` for search (`grep_search` tool).
- Use `mvn` for builds and tests.
- Use focused commands over broad full-repo checks.

### Phase 3: Implementation

Work acceptance-criteria first and subtask by subtask.

Execution model:

1. Analyze the ticket and affected files.
2. Implement one subtask or acceptance-criteria slice at a time.
3. Follow TDD: write the test first (RED), then implement (GREEN), then refactor (REFACTOR).
4. Verify each slice with the relevant test command.
5. Report progress clearly.
6. Only stop to ask the user if a decision is risky, ambiguous, or blocked by missing information.

Strict rules during implementation:

- NO hardcoded table or column names — all from metadata.
- NO SQL value concatenation — always use named bind parameters (`:p1`, `:p2`).
- NO Spring annotations in `domain/` layer.
- NO business logic in controllers.
- Do not introduce design patterns not listed in `CLAUDE.md` Active Design Patterns.
- ASK before adding any new dependency to `pom.xml`.

Do not wait for manual user verification between every small edit unless the user explicitly asks for that style.

### Phase 4: Verification

Run the narrowest relevant verification after each completed slice.

```bash
# Run a specific test class
mvn test -Dtest=SpecificTestClass

# Run all tests
mvn test

# Build the project
mvn clean compile
```

Record what was run and whether it passed.

### Phase 5: Completion

If the ticket has subtasks, complete them one by one.

For each subtask:

1. Implement only the scoped changes required.
2. Run focused verification (`mvn test -Dtest=RelevantTestClass`).
3. Create one dedicated commit following the commit-message-generator skill.
4. Add a Jira approval comment summarizing implementation and verification.
5. Transition the subtask to `DONE`.

After all subtasks are complete:

1. Verify the parent ticket scope is covered.
2. Add a parent summary comment.
3. Transition the parent ticket to `DONE`.

If there are no subtasks:

1. Verify the ticket scope is covered.
2. Create a focused commit.
3. Add a Jira approval comment.
4. Transition the ticket to `DONE`.

## Commit Guidance

Follow Conventional Commits and include the Jira key.

Examples:

```text
feat(engine): SCRUM-44 add BFS join resolver for multi-entity queries
fix(filter): SCRUM-55 correct BETWEEN parameter binding order
test(builder): SCRUM-60 add SelectClauseBuilder unit tests
refactor(metadata): SCRUM-70 extract field lookup to MetadataService
docs(api): SCRUM-80 update OpenAPI annotations for query endpoint
```

Keep commits scoped to one task or one subtask whenever practical.

## Checklist Template

```markdown
# Acceptance Criteria: [SCRUM-X] [Title]
- [ ] AC 1
- [ ] AC 2
- [ ] AC 3
```

## Common Mistakes to Avoid

- Do not assume Jira MCP is always live just because it is configured.
- Do not start editing before checking acceptance criteria and affected files.
- Do not mix unrelated dirty worktree files into a task commit.
- Do not use a broad refactor when a focused change satisfies the ticket.
- Do not close the parent ticket before finishing and closing its subtasks.
- Do not omit verification details from the Jira approval comment.
- Do not hardcode table or column names in Java — always use metadata.
- Do not add Spring annotations to the `domain/` layer.
- Do not skip writing tests before implementation (TDD is mandatory).

## vmetrix-query-manager Usage Example

Scenario: Picking up `SCRUM-100` — "Add IS_NOT_NULL comparator strategy".

1. Fetch the ticket and verify acceptance criteria.
2. Build a checklist from the ACs.
3. Identify target layer: `domain/engine/filter/` (new `IsNotNullStrategy`).
4. Verify it follows the Strategy pattern defined in `CLAUDE.md`.
5. Write the test first: `IsNotNullStrategyTest` with method `should_generate_is_not_null_sql_when_applied`.
6. Implement `IsNotNullStrategy` implementing `ComparatorStrategy`.
7. Register it in `ComparatorStrategyFactory`.
8. Run `mvn test -Dtest=IsNotNullStrategyTest` — verify GREEN.
9. Commit: `feat(filter): SCRUM-100 add IsNotNull comparator strategy`.
10. Add Jira approval comment and transition to `DONE`.
