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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<%
    pageContext.setAttribute("choose", "a",
                             PageContext.REQUEST_SCOPE);
 
%>

<f:view>
<html>
<head>
<title>jstl-choose test without "id"</title>
</head>
<body>
<h:outputText value="Begin jstl-choose test without id"/>
<c:choose>
  <c:when test="${requestScope.choose == 'a'}">
    <h:outputText  value="[FIRST]"/>
    <h:outputText  value="[SECOND]"/>
  </c:when>
  <c:when test="${requestScope.choose == 'b'}">
    <h:outputText value="[THIRD]"/>
    <h:outputText  value="[FOURTH]"/>
  </c:when>
  <c:otherwise>
    <h:outputText value="[FIFTH]"/>
    <h:outputText value="[SIXTH]"/>
  </c:otherwise>
</c:choose>
<h:outputText value="End jstl-choose test without id"/>
</body>
</html>
</f:view>
