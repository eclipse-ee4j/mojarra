@echo off

::    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
::
::    This program and the accompanying materials are made available under the
::    terms of the Eclipse Public License v. 2.0, which is available at
::    http://www.eclipse.org/legal/epl-2.0.
::
::    This Source Code may also be made available under the following Secondary
::    Licenses when the conditions for such availability set forth in the
::    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
::    version 2 with the GNU Classpath Exception, which is available at
::    https://www.gnu.org/software/classpath/license.html.
::
::    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

echo.
echo Running tests 
echo.
echo.
echo.
echo Note that this should be run from the [mojarra home]\test folder as .\bin\run-tests.bat
echo You can specify specific tests by passing it as argument eg .\bin\run-tests.bat javaee8
echo.
echo.

if [%1] == [] (
    set arr=unit servlet30 servlet31 servlet40 javaee6web javaee6 javaee7 javaee8
    echo.
    echo No tests specified as command arguments, using default set
    echo.
) else (
    set arr=%*
)

echo.
echo Running tests for:
echo.
echo.
echo %arr%
echo.

set top_dir=%cd%

echo.
echo Running tests from %top_dir%

for %%i in (%arr%) do (

   echo.
   echo.
   echo.
   echo.
   echo.
   echo **************************************
   echo  Descending into %%i
   echo **************************************
   echo.
   echo.
   echo.
   echo.
   echo.

   cd %%i
   call %top_dir%\bin\test-glassfish-default.bat
   set exit_code=%ERRORLEVEL%

   echo.
   echo.
   echo.
   echo.
   echo.
   echo **************************************
   echo  Finished testing %%i
   echo **************************************
   echo.
   echo.
   echo.
   echo.
   echo.

   cd %top_dir%

   echo Back at %cd%
   echo.

   if %exit_code% neq 0 ( 
       echo.
       echo.
       echo.
       echo.
       echo EXITING BECAUSE OF FAILURES. SEE ABOVE!
       echo.
       echo.
       echo.
       echo.
       exit /b
   )

)