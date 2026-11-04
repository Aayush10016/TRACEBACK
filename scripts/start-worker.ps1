. "$PSScriptRoot/common.ps1"
Import-LocalEnvironment
$temp=Set-ProjectTemp
if (!$env:WORKER_USERNAME -or $env:WORKER_USERNAME.Contains(':') -or !$env:WORKER_PASSWORD -or $env:WORKER_PASSWORD.Length -lt 16) { throw 'Set WORKER_USERNAME and WORKER_PASSWORD (16+ characters) in D:\TRACEBACK\.env first.' }
if(Get-NetTCPConnection -LocalPort 8085 -State Listen -ErrorAction SilentlyContinue) { throw 'Port 8085 is already in use. Check the existing worker before starting another.' }
docker info --format '{{.ServerVersion}}' | Out-Null
if($LASTEXITCODE -ne 0) { throw 'Start Docker Desktop first.' }
docker image inspect traceback-java-runner:17 --format '{{.Id}}' | Out-Null
if($LASTEXITCODE -ne 0) { throw 'Run scripts/build-runner.ps1 first.' }
$jar=Join-Path $ProjectRoot 'backend/target/traceback-1.0.0.jar'
if(!(Test-Path -LiteralPath $jar)){throw 'Build the release first.'}
$names=@('DEMO_USERNAME','DEMO_PASSWORD','NVIDIA_API_KEY','META_API_KEY','PROJECT_WORKER_URL','DEMO_PUBLIC_ORIGIN','PROJECT_STORAGE','SPRING_PROFILES_ACTIVE')
$previous=@{}
foreach($name in $names){$previous[$name]=[Environment]::GetEnvironmentVariable($name,'Process')}
try {
 $env:DEMO_USERNAME=$env:WORKER_USERNAME;$env:DEMO_PASSWORD=$env:WORKER_PASSWORD
 $env:NVIDIA_API_KEY='';$env:META_API_KEY='';$env:PROJECT_WORKER_URL='';$env:DEMO_PUBLIC_ORIGIN=''
 $env:PROJECT_STORAGE=Join-Path $ProjectRoot 'backend/data/worker/projects'
 $env:SPRING_PROFILES_ACTIVE='local'
 $java='C:\Program Files\Java\jdk-17\bin\java.exe'
 $process=Start-Process -FilePath $java -ArgumentList @('-Xmx384m',"-Djdk.net.unixdomain.tmpdir=$temp",'-jar',$jar,'--server.address=127.0.0.1','--server.port=8085','--traceback.demo.access-enabled=true','--spring.datasource.url=jdbc:h2:file:./data/worker/traceback;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH') -WorkingDirectory (Join-Path $ProjectRoot 'backend') -WindowStyle Hidden -RedirectStandardOutput (Join-Path $ProjectRoot '.tools/worker.log') -RedirectStandardError (Join-Path $ProjectRoot '.tools/worker-error.log') -PassThru
 Write-Output "Worker started independently (PID $($process.Id)), port 8085. Check /actuator/health before starting the tunnel."
} finally {foreach($name in $names){[Environment]::SetEnvironmentVariable($name,$previous[$name],'Process')}}
