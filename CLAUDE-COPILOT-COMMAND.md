Convenience: Copilot "claude" command

This repository includes a small PowerShell wrapper at scripts\claude-wrap.ps1 that pipes a file into the local `claude` CLI and applies project settings from .claude/settings.json when available.

Quick usage:
- From PowerShell:   .\scripts\claude-wrap.ps1 .\README.md
- From Copilot CLI:  ! .\scripts\claude-wrap.ps1 .\README.md

If you'd like a true Copilot CLI slash command (e.g., typing `/claude README.md`), that needs a personal Copilot config or plugin on your machine. I can:
- Add a per-user alias instructions file for you to install, or
- Create a small local plugin to expose `/claude` (will require you to enable it locally).

Tell me which option you prefer and I'll implement it.