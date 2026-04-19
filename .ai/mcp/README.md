# MCP Setup Instructions

This folder contains templates for configuring the Model Context Protocol (MCP) environment for the `vmetrix-query-manager` project.

## Overview

The MCP environment allows AI agents to interact with external tools such as Jira, the local filesystem, and databases. To prevent security leaks, the main configuration file `.ai/mcp.json` is partially localized and contains secrets that should not be shared.

## Setup Steps

### 1. Create your local `mcp.json`

1. Copy the template to the `.ai/` root:
   ```bash
   cp .ai/mcp/mcp.json.template .ai/mcp.json
   ```
2. Open `.ai/mcp.json` and replace the following placeholders:
   - `[ABSOLUTE_PATH_TO_REPO]`: The full path to this repository on your machine (e.g., `C:/Users/name/Dev/vmetrix-query-manager`).
   - `[YOUR_JIRA_API_KEY]`: Your Atlassian API token.
   - `[YOUR_EMAIL]`: Your Atlassian account email.
   - `[YOUR_INSTANCE]`: Your Atlassian site name (e.g., `company.atlassian.net`).

### 2. Configure Secrets (Optional)

If you prefer using an environment file for some servers, you can use `.env.mcp.template` as a reference to create a `.env` file in the project root. Note that many MCP servers expect these values either directly in the `mcp.json` file or as system environment variables.

### 3. Verification

Once `.ai/mcp.json` is configured, your AI agent should be able to:
- Read/Write files (via `filesystem` server).
- Read/Create Jira tasks (via `jira` server).
- Execute SQL queries (via `postgres` server).

---

> [!WARNING]
> **Security Reminder:** Never commit your personal `.ai/mcp.json` if it contains plain-text API keys or passwords. Ensure it is ignored by git if necessary, or carefully use placeholders in shared templates.
