@echo off
REM ====================================================================
REM Retail Microservices - Run End-to-End Tests
REM ====================================================================

echo ====================================================================
echo Starting Retail Microservices End-to-End Test Suite
echo ====================================================================

node "%~dp0\e2e-test-runner.js" %*

