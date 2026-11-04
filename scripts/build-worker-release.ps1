. "$PSScriptRoot/common.ps1"
$maven=Get-ProjectMaven
Set-ProjectTemp | Out-Null
Push-Location (Join-Path $ProjectRoot 'backend')
try {
 & $maven "-Dmaven.repo.local=$ProjectRoot/.tools/m2" "-Dtraceback.build-dir=$ProjectRoot/.tools/worker-release" --no-transfer-progress verify
 if($LASTEXITCODE -ne 0){throw 'Worker release verification failed.'}
} finally {Pop-Location}
