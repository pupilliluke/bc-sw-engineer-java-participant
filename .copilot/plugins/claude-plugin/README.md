Repo-local Copilot plugin: claude-local

Purpose:
Expose a simple `/claude <path>` command that runs the repo-local `scripts\claude-wrap.ps1` wrapper and uses the project's .claude settings if present.

Enable locally (examples):
- Temporary (single run):
    claude --plugin-dir ./.copilot/plugins/claude-plugin

- Persistent (recommended):
  Add the plugin directory to your personal Copilot CLI config or your shell alias that launches claude, for example in PowerShell profile:
    function claude-with-plugins { claude --plugin-dir "${env:PWD}\\.copilot\\plugins\\claude-plugin" @Args }

Usage from Copilot CLI once plugin is loaded:
  /claude README.md

Security:
- This plugin runs local scripts and uses your local Claude auth — do NOT commit sensitive files from .claude
- The plugin is repository-local and will not be loaded automatically unless you start Claude with --plugin-dir pointing at it.

If you'd like, I can also add an install script that wires this up in your PowerShell profile.
