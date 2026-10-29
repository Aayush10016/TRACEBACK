$ErrorActionPreference='Stop'
$ProjectRoot=Split-Path -Parent $PSScriptRoot
function Import-LocalEnvironment {
 $path=Join-Path $ProjectRoot '.env'
 if(Test-Path -LiteralPath $path){foreach($line in Get-Content -LiteralPath $path){if($line -match '^\s*([A-Z][A-Z0-9_]*)\s*=(.*)$'){ $name=$Matches[1];$value=$Matches[2].Trim();if($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))){$value=$value.Substring(1,$value.Length-2)};[Environment]::SetEnvironmentVariable($name,$value,'Process') }}}
}
function Get-ProjectMaven {
 $maven=Join-Path $ProjectRoot '.tools/apache-maven-3.9.16/bin/mvn.cmd'
 if(!(Test-Path -LiteralPath $maven)){
  $tools=Join-Path $ProjectRoot '.tools';New-Item -ItemType Directory -Path $tools -Force|Out-Null
  $archive=Join-Path $tools 'maven.zip';$url='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.zip'
  Invoke-WebRequest -Uri $url -OutFile $archive
  $expected=(Invoke-RestMethod -Uri ($url+'.sha512')).Trim().Split(' ')[0]
  if((Get-FileHash -LiteralPath $archive -Algorithm SHA512).Hash -ne $expected){throw 'Maven archive checksum mismatch.'}
  Expand-Archive -LiteralPath $archive -DestinationPath $tools -Force
 }
 return $maven
}
function Set-ProjectTemp {
 $temp=Join-Path $ProjectRoot '.tools/tmp';New-Item -ItemType Directory -Path $temp -Force|Out-Null
 $env:TEMP=$temp;$env:TMP=$temp
 return $temp
}
