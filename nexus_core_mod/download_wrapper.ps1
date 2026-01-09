$ErrorActionPreference = "Stop"
$url = "https://raw.githubusercontent.com/gradle/gradle/v8.1.1/gradle/wrapper/gradle-wrapper.jar"
$output = "gradle/wrapper/gradle-wrapper.jar"
$dir = "gradle/wrapper"

if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Force -Path $dir
}

Write-Host "Downloading gradle-wrapper.jar..."
Invoke-WebRequest -Uri $url -OutFile $output
Write-Host "Download complete: $output"
