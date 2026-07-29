param(
  [Parameter(ValueFromRemainingArguments=$true)][string[]]$Args
)

if (-not $Args -or $Args.Count -eq 0) {
  Write-Output "Usage: /claude <path>"
  exit 1
}

$path = $Args -join ' '
$resolved = Resolve-Path -Path $path -ErrorAction SilentlyContinue
if (-not $resolved) {
  Write-Error "File not found: $path"
  exit 1
}

# Call the repo-local wrapper script
$repoRoot = (Get-Location).Path
$wrap = Join-Path $repoRoot 'scripts\claude-wrap.ps1'
if (-not (Test-Path $wrap)) {
  Write-Error "Wrapper script not found at $wrap. Use .\\scripts\\claude-wrap.ps1"
  exit 1
}

& $wrap $resolved.Path
