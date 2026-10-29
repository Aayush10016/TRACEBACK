$ErrorActionPreference='Stop'
Push-Location (Join-Path (Split-Path -Parent $PSScriptRoot) 'frontend')
try {if(!(Test-Path -LiteralPath 'node_modules')){npm ci;if($LASTEXITCODE -ne 0){throw 'Frontend installation failed.'}};npm run dev;if($LASTEXITCODE -ne 0){throw 'Frontend exited with an error.'}}finally{Pop-Location}
