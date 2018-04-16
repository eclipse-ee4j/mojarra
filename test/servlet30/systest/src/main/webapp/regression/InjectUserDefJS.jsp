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

<%@ page contentType="text/html" language="java" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<%--
   Regression info:
      Ensure rows attribute is rendered when using the 
      HTML text area component.  
   Issue 312: https://javaserverfaces.dev.java.net/issues/show_bug.cgi?id=312
--%>

<html>
    <head>
        <title>User Defined JS Injection Test</title>        
    </head>
    <body>
        <f:view>
            <h:form>
                <h:commandLink value="Link" onclick="alert('Are you sure?')"/>
                <h:commandButton value="Button" onclick="alert('Are you sure?')"/>
            </h:form>
        </f:view>
    </body>
</html>
