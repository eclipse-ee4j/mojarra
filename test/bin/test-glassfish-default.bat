call %~dp0\test-glassfish-specific.bat Production true server
if not "%ERRORLEVEL%" == "0" exit /b
