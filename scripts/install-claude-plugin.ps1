$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$pluginDir = Resolve-Path (Join-Path $scriptDir '..\.copilot\plugins\claude-plugin') -ErrorAction SilentlyContinue
if (-not $pluginDir) {
  Write-Error "Plugin not found at $scriptDir\\..\\.copilot\\plugins\\claude-plugin"
  exit 1
}
$pluginPath = $pluginDir.Path

$profilePath = $PROFILE
if (-not (Test-Path $profilePath)) {
  New-Item -ItemType File -Path $profilePath -Force | Out-Null
}

$marker = "# Added by claude-plugin installer"
$functionText = @"
$marker
function claude-with-plugins {
  param([Parameter(ValueFromRemainingArguments=\$true)] \$args)
  claude --plugin-dir \"$pluginPath\" @args
}
"@

$existing = Get-Content $profilePath -Raw -ErrorAction SilentlyContinue
if ($existing -notmatch [regex]::Escape($marker)) {
  Add-Content -Path $profilePath -Value $functionText
  Write-Output "Added claude-with-plugins to $profilePath"
  Write-Output "Run: . $profilePath  (or restart your shell) to load the function now."
} else {
  Write-Output "Profile already contains claude-with-plugins; no changes made."
}

Write-Output "Installer script completed."
