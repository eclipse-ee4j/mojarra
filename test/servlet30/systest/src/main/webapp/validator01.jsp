<%--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

--%>

<%@ page contentType="text/html"
%><%@ page import="jakarta.faces.FactoryFinder"
%><%@ page import="jakarta.faces.application.Application"
%><%@ page import="jakarta.faces.application.ApplicationFactory"
%><%@ page import="jakarta.faces.validator.Validator"
%><%@ page import="com.sun.faces.systest.TestValidator"
%><%

  // Acquire our Application instance
  ApplicationFactory afactory = (ApplicationFactory)
   FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
  Application appl = afactory.getApplication();

  // replace mappings provided by the JSF implementation for Length
  // LengthValidator and make sure that it can be retrieved using the
  // standard identifier.
  appl.addValidator("Length", "com.sun.faces.systest.TestValidator");

  // try to retrieve our component from Application
  Validator result = appl.createValidator("Length");
  // report the result
  if (result == null || 
      !(result instanceof com.sun.faces.systest.TestValidator)) {
    out.println("/validator01.jsp FAILED");
    return;
  } else {
      out.println("/validator01.jsp PASSED");
  }
  
%>
