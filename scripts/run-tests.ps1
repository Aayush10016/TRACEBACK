. "$PSScriptRoot/common.ps1"
$maven=Get-ProjectMaven
Set-ProjectTemp | Out-Null
Push-Location (Join-Path $ProjectRoot 'backend')
try {& $maven "-Dmaven.repo.local=$ProjectRoot/.tools/m2" --no-transfer-progress clean verify;if($LASTEXITCODE -ne 0){throw 'Backend verification failed.'}}finally{Pop-Location}
Push-Location (Join-Path $ProjectRoot 'frontend')
try {if(!(Test-Path -LiteralPath 'node_modules')){npm ci;if($LASTEXITCODE -ne 0){throw 'Dependency installation failed.'}};foreach($task in @('test','lint','build')){npm run $task;if($LASTEXITCODE -ne 0){throw "Frontend $task failed."}}}finally{Pop-Location}
