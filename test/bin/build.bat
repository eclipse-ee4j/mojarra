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
echo Building Mojarra and installing in Maven
echo This should be run every time after source code changes are made
echo.
echo.
echo.
echo Note that this should be run from the [mojarra home]\test folder as .\bin\build.bat
echo.
echo.

call cd ..

echo Running initial ant build from %cd%

:: ant main clean main after release, e.g. m05 changed to m06
call ant clean main

if %ERRORLEVEL% neq 0 (
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

echo.
echo Running ant initiated maven build and install
echo.
echo.

call ant mvn.deploy.snapshot.local

if %ERRORLEVEL% neq 0 ( 
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

call cd test