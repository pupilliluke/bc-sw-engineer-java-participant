$inner = @(
    "src\main\java\com\northstar\crm\repository\*.java",
    "src\main\java\com\northstar\crm\entity\*.java"
)
$bad = Select-String -Path $inner -Pattern "import\s+com\.northstar\.crm\.controller"
if ($bad) {
    $bad | ForEach-Object { Write-Output "$($_.Path):$($_.LineNumber)  $($_.Line.Trim())" }
    Write-Output "FAIL layer rule broken, controller imported from an inner layer"
    exit 1
}
Write-Output "PASS no controller imports under repository/ or entity/"
exit 0
