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
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:view>
<html>
<head>
<title>subview04</title>
</head>
<body>
<p>
<h:outputText value="[A]"/></p>

<f:subview id="foo02">
<p><h:outputText value="Begin test <c:include> with subview tag in including page"/></p>

<p><jsp:include page="bar01.jsp"/></p>
</f:subview>

<p><h:outputText value="subview04"/></p>

<f:subview id="bar02">
<p><jsp:include page="bar02.jsp"/></p>
</f:subview>

<p><h:outputText value="End test <c:include> with subview tag in including page"/></p>
</body>
</html>
</f:view>
