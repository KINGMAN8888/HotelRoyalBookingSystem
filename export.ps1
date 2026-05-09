# Hotel Royal — Export to Standalone Desktop Application
# This script compiles, packages all JARs into one fat JAR, and creates a launcher BAT.

$jdk   = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'
$src   = 'F:\Java Project\HotelSystem\HotelBookingSystem\src'
$out   = 'F:\Java Project\HotelSystem\HotelBookingSystem\build\classes'
$libs  = 'F:\Java Project\HotelSystem\HotelBookingSystem\libs'
$dist  = 'F:\Java Project\dist'
$jar   = "$dist\HotelRoyal.jar"
$cp    = "$libs\flatlaf-3.4.jar;$libs\pdfbox-app-2.0.30.jar;$libs\AbsoluteLayout.jar;$libs\mysql-connector-j-9.7.0.jar"

Write-Host "Step 1: Cleaning build output..."
Remove-Item -Recurse -Force $out -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $out | Out-Null
New-Item -ItemType Directory -Force -Path $dist | Out-Null

Write-Host "Step 2: Compiling source files..."
$files = Get-ChildItem -Path $src -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
$argfile = "$env:TEMP\sources_export.txt"
$files | ForEach-Object { '"' + $_.Replace('\', '/') + '"' } | Set-Content $argfile -Encoding ASCII

& "$jdk\bin\javac" --release 11 -cp $cp -d $out "@$argfile" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation FAILED. Aborting export." -ForegroundColor Red
    exit 1
}
Write-Host "  Compilation successful." -ForegroundColor Green

Write-Host "Step 2b: Copying image and resource files into build output..."
$resourceExtensions = @("*.png", "*.jpg", "*.jpeg", "*.gif", "*.ico", "*.properties", "*.xml")
foreach ($ext in $resourceExtensions) {
    $resources = Get-ChildItem -Path $src -Recurse -Filter $ext
    foreach ($res in $resources) {
        $relPath = $res.FullName.Substring($src.Length + 1)
        $destPath = Join-Path $out $relPath
        $destDir = Split-Path $destPath -Parent
        if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Force -Path $destDir | Out-Null }
        Copy-Item -Path $res.FullName -Destination $destPath -Force
    }
}
Write-Host "  Resources copied." -ForegroundColor Green

Write-Host "Step 3: Extracting dependency JARs into build output..."
$depJars = @(
    "$libs\flatlaf-3.4.jar",
    "$libs\pdfbox-app-2.0.30.jar",
    "$libs\AbsoluteLayout.jar",
    "$libs\mysql-connector-j-9.7.0.jar"
)

foreach ($dep in $depJars) {
    Write-Host "  Extracting: $(Split-Path $dep -Leaf)"
    Push-Location $out
    & "$jdk\bin\jar" xf $dep 2>&1
    Pop-Location
}

Write-Host "Step 4: Removing META-INF signature files to avoid security conflicts..."
Remove-Item -Force "$out\META-INF\*.SF"  -ErrorAction SilentlyContinue
Remove-Item -Force "$out\META-INF\*.RSA" -ErrorAction SilentlyContinue
Remove-Item -Force "$out\META-INF\*.DSA" -ErrorAction SilentlyContinue

Write-Host "Step 5: Creating manifest..."
$manifest = @"
Manifest-Version: 1.0
Main-Class: hotelbookingsystem.HotelBookingSystem
"@
$manifestPath = "$env:TEMP\MANIFEST_EXPORT.MF"
$manifest | Set-Content $manifestPath -Encoding ASCII

Write-Host "Step 6: Packaging fat JAR..."
Push-Location $out
& "$jdk\bin\jar" cfm $jar $manifestPath . 2>&1
Pop-Location

if ($LASTEXITCODE -ne 0) {
    Write-Host "JAR packaging FAILED." -ForegroundColor Red
    exit 1
}
Write-Host "  Fat JAR created: $jar" -ForegroundColor Green

Write-Host "Step 7: Creating launcher BAT file..."
$bat = @"
@echo off
title Hotel Royal
cd /d "%~dp0"
"C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot\bin\java" -jar HotelRoyal.jar
pause
"@
$bat | Set-Content "$dist\HotelRoyal.bat" -Encoding ASCII
Write-Host "  Launcher created: $dist\HotelRoyal.bat" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Export Complete!" -ForegroundColor Cyan
Write-Host " Output directory: $dist"
Write-Host " Run: HotelRoyal.bat (double-click)"
Write-Host "========================================" -ForegroundColor Cyan
