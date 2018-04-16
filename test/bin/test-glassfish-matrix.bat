call %~dp0\test-glassfish-specific.bat Development true server
if not "%ERRORLEVEL%" == "0" exit /b

call %~dp0\test-glassfish-specific.bat Development true client
if not "%ERRORLEVEL%" == "0" exit /b

call %~dp0\test-glassfish-specific.bat Development false server
if not "%ERRORLEVEL%" == "0" exit /b

call %~dp0\test-glassfish-specific.bat Development false client
if not "%ERRORLEVEL%" == "0" exit /b

call %~dp0\test-glassfish-specific.bat Production true server
if not "%ERRORLEVEL%" == "0" exit /b

call %~dp0\test-glassfish-specific.bat Production true client
if not "%ERRORLEVEL%" == "0" exit /b

call %~dp0\test-glassfish-specific.bat Production false server
if not "%ERRORLEVEL%" == "0" exit /b

call %~dp0\test-glassfish-specific.bat Production false client
if not "%ERRORLEVEL%" == "0" exit /b
