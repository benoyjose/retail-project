@echo off
echo ========================================================
echo Building Retail Microservices System (18 Modules)
echo ========================================================
cd ..
call mvn clean install
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven build failed.
    exit /b %ERRORLEVEL%
)
echo [SUCCESS] All 18 modules compiled, tested, and packaged successfully!
pause

