$jdk = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'
$src = 'F:\Java Project\HotelSystem\HotelBookingSystem\src'
$out = 'F:\Java Project\HotelSystem\HotelBookingSystem\build\classes'
$libs = 'F:\Java Project\HotelSystem\HotelBookingSystem\libs'
$cp = $libs + '\flatlaf-3.4.jar;' + $libs + '\pdfbox-app-2.0.30.jar;' + $libs + '\AbsoluteLayout.jar;' + $libs + '\mysql-connector-j-9.7.0.jar'

New-Item -ItemType Directory -Force -Path $out | Out-Null

# Regenerate sources list with forward slashes and quoted paths
$files = Get-ChildItem -Path $src -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
$argfile = 'F:\Java Project\sources.txt'
$files | ForEach-Object { '"' + $_.Replace('\', '/') + '"' } | Set-Content $argfile -Encoding ASCII

& "$jdk\bin\javac" --release 11 -cp $cp -d $out "@$argfile" 2>&1
Write-Host "ExitCode: $LASTEXITCODE"
