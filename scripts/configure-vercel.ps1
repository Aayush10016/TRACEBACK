param([Parameter(Mandatory=$true)][string]$BackendUrl)
$ErrorActionPreference='Stop'
$uri=[Uri]$BackendUrl
if($uri.Scheme -ne 'https' -or !$uri.Host.EndsWith('.onrender.com') -or $uri.UserInfo -or $uri.Query -or $uri.Fragment -or $uri.AbsolutePath -ne '/') { throw 'Use the HTTPS Render origin only, e.g. https://traceback-xxxx.onrender.com' }
$config=@{framework='vite';buildCommand='npm run build -- --mode hosted';outputDirectory='dist';rewrites=@(@{source='/api/:path*';destination=($BackendUrl.TrimEnd('/')+'/api/:path*')})}
$path=Join-Path (Split-Path -Parent $PSScriptRoot) 'frontend/vercel.json'
$config | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $path -Encoding utf8
Write-Output 'Vercel configuration written. Commit and push frontend/vercel.json before importing the frontend.'
