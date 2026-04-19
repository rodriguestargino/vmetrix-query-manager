# Agents

Agent definitions live in this folder.

## Conventions

- One file per agent: `<agent-name>.md`
- File name is the canonical agent identifier (kebab-case).
- Each agent file should declare:
  - **Purpose** — one-line description of what the agent does.
  - **When to use** — triggers and scenarios.
  - **Tools / capabilities** — what the agent can call.
  - **Constraints** — what the agent must never do.
  - **Repo alignment** — reference to `CLAUDE.md` rules the agent must follow.

## Example file stub

```markdown
# Agent: <Agent Name>

## Purpose
<one line>

## When to Use
- <trigger 1>
- <trigger 2>

## Capabilities
- <tool / action 1>
- <tool / action 2>

## Constraints
- <never do X>
- <always do Y>

## Repo Alignment
Follows the rules in ../../CLAUDE.md.
```
