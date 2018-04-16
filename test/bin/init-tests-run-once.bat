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
echo Installing tests 
echo This only needs to be run once for every release, e.g. m05 changed to m06
echo.
echo.
echo.
echo Note that this should be run from the [mojarra home]\test folder as .\bin\init-tests-run-once.bat
echo.
echo.

call mvn clean install
