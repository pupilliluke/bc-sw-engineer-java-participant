param(
  [Parameter(Mandatory=$true)][string]$Path,
  [string]$Prompt = "Please summarize and suggest improvements for the following file."
)

if (-not (Test-Path $Path)) {
  Write-Error "File not found: $Path"
  exit 1
}

$projectSettings = Join-Path (Get-Location) ".claude\settings.json"
if (Test-Path $projectSettings) {
  Get-Content $Path -Raw | claude -p $Prompt --settings $projectSettings
} else {
  Get-Content $Path -Raw | claude -p $Prompt
}

# Usage: .\scripts\claude-wrap.ps1 .\README.md
# From Copilot CLI: run `! .\\scripts\\claude-wrap.ps1 .\\README.md` to invoke from the / prompt
