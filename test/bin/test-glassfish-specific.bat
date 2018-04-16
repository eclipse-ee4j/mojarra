@echo *************************************************************************
@echo *
@echo *  Test for (%1, %2, %3) 
@echo *
@echo *************************************************************************

call mvn -N -Pglassfish-patch validate
if not "%ERRORLEVEL%" == "0" exit /b

call mvn -Dwebapp.projectStage=%1 -Dwebapp.partialStateSaving=%2 -Dwebapp.stateSavingMethod=%3 clean install
if not "%ERRORLEVEL%" == "0" exit /b

call mvn -N -Pglassfish-cargo -Dwebapp.projectStage=%1 -Dwebapp.partialStateSaving=%2 -Dwebapp.stateSavingMethod=%3 cargo:start
if not "%ERRORLEVEL%" == "0" exit /b

call mvn -Pglassfish-cargo -Dwebapp.projectStage=%1 -Dwebapp.partialStateSaving=%2 -Dwebapp.stateSavingMethod=%3 cargo:redeploy
if not "%ERRORLEVEL%" == "0" (
    call mvn -N -Pglassfish-cargo -Dwebapp.projectStage=%1 -Dwebapp.partialStateSaving=%2 -Dwebapp.stateSavingMethod=%3 cargo:stop 
    exit /b
)

call mvn -Pintegration -Dwebapp.projectStage=%1 -Dwebapp.partialStateSaving=%2 -Dwebapp.stateSavingMethod=%3 verify 
if not "%ERRORLEVEL%" == "0" (
    call mvn -N -Pglassfish-cargo -Dwebapp.projectStage=%1 -Dwebapp.partialStateSaving=%2 -Dwebapp.stateSavingMethod=%3 cargo:stop 
    exit /b
)

call mvn -N -Pglassfish-cargo -Dwebapp.projectStage=%1 -Dwebapp.partialStateSaving=%2 -Dwebapp.stateSavingMethod=%3 cargo:stop
if not "%ERRORLEVEL%" == "0" exit /b
