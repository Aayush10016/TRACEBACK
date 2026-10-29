param([ValidateSet('local','postgres')][string]$Profile='local')
. "$PSScriptRoot/common.ps1"
Import-LocalEnvironment
$maven=Get-ProjectMaven
$temp=Set-ProjectTemp
Push-Location (Join-Path $ProjectRoot 'backend')
try {
 & $maven "-Dmaven.repo.local=$ProjectRoot/.tools/m2" --no-transfer-progress -DskipTests package
 if($LASTEXITCODE -ne 0){throw 'Backend packaging failed.'}
 & java "-Djdk.net.unixdomain.tmpdir=$temp" -jar target/traceback-1.0.0.jar "--spring.profiles.active=$Profile"
 if($LASTEXITCODE -ne 0){throw 'Backend process exited with an error.'}
} finally {Pop-Location}
