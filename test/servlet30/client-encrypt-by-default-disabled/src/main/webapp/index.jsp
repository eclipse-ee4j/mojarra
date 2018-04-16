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



<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xml:lang="en" lang="en">
<jsp:output doctype-root-element="html"
            doctype-public="-//W3C//DTD XHTML 1.0 Trasitional//EN"
            doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
<jsp:directive.page contentType="application/xhtml+xml; charset=UTF-8"/>
<head>
</head>
<body>

<!--  

This page allows the user to go to the context-path and get redirected
to the front page of the app.  For example,
http://localhost:8080/jsf-carstore/.  Note that we use "*.jsf" as the
page mapping.  Doing so allows us to just name our pages as "*.jsp",
refer to them as "*.jsf" and know that they will be properly picked up
by the container.

-->

<jsp:forward page="guess/greeting.jsp"/>
</body>
</html>
