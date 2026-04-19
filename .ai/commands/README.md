# Commands

Custom slash commands and reusable prompt templates live in this folder.

## Conventions

- One file per command: `<command-name>.md`
- File name is the canonical command identifier (kebab-case).
- Each command file should declare:
  - **Name** — the invocation identifier.
  - **Purpose** — one-line description.
  - **Arguments** — inputs the command accepts.
  - **Prompt template** — the body sent to the assistant.

## Example file stub

```markdown
# Command: <command-name>

## Purpose
<one line>

## Arguments
- `<arg1>` — description
- `<arg2>` — description

## Prompt Template
<prompt body with {{placeholders}}>
```
