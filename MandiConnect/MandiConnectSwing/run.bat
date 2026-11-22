@echo off
if not exist "%~dp0target\mandiconnect-swing-1.0.0.jar" (
    echo JAR not found. Building project first...
    call "%~dp0build.bat"
)
echo Starting MandiConnect Application...
java -jar "%~dp0target\mandiconnect-swing-1.0.0.jar"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error. Ensure XAMPP MySQL is running!
    pause
)
