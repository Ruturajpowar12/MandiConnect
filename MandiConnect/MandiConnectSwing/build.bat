@echo off
echo ==============================================
echo Building MandiConnect Swing Application...
echo ==============================================
call "%~dp0mvnw.cmd" clean package
echo.
echo Build complete! Run run.bat to start the app.
pause
