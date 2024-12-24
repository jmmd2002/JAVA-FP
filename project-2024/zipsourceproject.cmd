@echo off

for /F "delims=" %%i in ("%CD%") do set PROJECT=%%~nxi
set filename=%PROJECT%-%USERNAME%.zip
cd ..


tar.exe -a -c -f %PROJECT%\%filename% %PROJECT%\src %PROJECT%\.project %PROJECT%\.classpath %PROJECT%\README

echo "File %filename% created."
pause