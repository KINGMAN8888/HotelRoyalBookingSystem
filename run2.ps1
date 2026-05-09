$jdk = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'
$out = 'F:\Java Project\HotelSystem\HotelBookingSystem\build\classes'
$libs = 'F:\Java Project\HotelSystem\HotelBookingSystem\libs'
$cp = '"' + $out + ';' + $libs + '\flatlaf-3.4.jar;' + $libs + '\pdfbox-app-2.0.30.jar;' + $libs + '\AbsoluteLayout.jar;' + $libs + '\mysql-connector-j-9.7.0.jar"'

$args = "-cp $cp hotelbookingsystem.HotelBookingSystem"
Start-Process -FilePath "$jdk\bin\java" -ArgumentList $args -Wait -NoNewWindow -RedirectStandardError "F:\Java Project\run_err.txt" -RedirectStandardOutput "F:\Java Project\run_out.txt"
Get-Content "F:\Java Project\run_err.txt" -ErrorAction SilentlyContinue
Get-Content "F:\Java Project\run_out.txt" -ErrorAction SilentlyContinue
