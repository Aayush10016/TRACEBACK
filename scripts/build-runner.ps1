. "$PSScriptRoot/common.ps1"
& docker info --format '{{.ServerVersion}}'
if ($LASTEXITCODE -ne 0) { throw 'Start Docker Desktop, wait for its engine, then run this script again.' }
Push-Location $ProjectRoot
try { & docker build -f Dockerfile.runner -t traceback-java-runner:17 .; if ($LASTEXITCODE -ne 0) { throw 'Java test runner build failed.' } } finally { Pop-Location }
