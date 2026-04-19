# .ai — AI Assistance Assets

Tool-agnostic home for every AI assistance artifact in this repository.

## Layout

```
.ai/
├── agents/                     Agent definitions (one .md file per agent)
├── skills/                     Reusable skills (one folder per skill)
│   └── <skill-name>/
│       └── SKILL.md
├── commands/                   Custom slash commands / prompt templates
└── mcp.json                    Model Context Protocol server configuration
```

## Conventions

- **Skills** live in `skills/<skill-name>/SKILL.md`. One folder per skill keeps
  any supporting assets (examples, snippets, scripts) co-located with the skill.
- **Agents** live in `agents/<agent-name>.md`. File name matches the agent name.
- **Commands** live in `commands/<command-name>.md` and represent reusable
  prompt templates or slash commands.
- **MCP servers** are declared in `mcp.json` under the `mcpServers` key.

## Related

- `../CLAUDE.md` — project contract (architecture, strict rules, naming).
  AI assistance artifacts in this folder must respect the rules defined there.
