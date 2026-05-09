@echo off
title Hotel Booking System

set JDK=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
set SRC=F:\Java Project\HotelSystem\HotelBookingSystem\src
set OUT=F:\Java Project\HotelSystem\HotelBookingSystem\build\classes
set LIBS=F:\Java Project\HotelSystem\HotelBookingSystem\libs
set CP=%OUT%;%LIBS%\AbsoluteLayout.jar;%LIBS%\mysql-connector-j-9.7.0.jar

echo.
echo ============================
echo  Hotel Booking System
echo  Building...
echo ============================
echo.

mkdir "%OUT%" 2>nul

powershell -ExecutionPolicy Bypass -File "F:\Java Project\compile3.ps1"

if %ERRORLEVEL% neq 0 (
    echo.
    echo Build FAILED. Press any key to exit.
    pause >nul
    exit /b 1
)

echo.
echo ============================
echo  Running...
echo ============================
echo.

"%JDK%\bin\java" -cp "%CP%" hotelbookingsystem.HotelBookingSystem

pause
