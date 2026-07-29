Slash command: /claude <path>

Description:
Runs the repo-local wrapper scripts\claude-wrap.ps1 using the local Claude CLI and the project's .claude settings (if present).

How to run from Copilot CLI:
- Use the shell-run prefix to invoke the PowerShell script:
  ! .\scripts\claude-wrap.ps1 .\path\to\file

Example:
  ! .\scripts\claude-wrap.ps1 .\README.md

Notes:
- This uses the local `claude` CLI and its auth context — no API keys required.
- Avoid committing sensitive files from .claude if they contain secrets.
