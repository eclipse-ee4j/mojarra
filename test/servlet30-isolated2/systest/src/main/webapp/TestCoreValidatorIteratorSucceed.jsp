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

<html>
<title>Validator Test Page</title>
<head>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
</head>
<body>

<h1>TLV c:iterator with JSF id</h1>
This page should succeed.
<br>
<br>

<f:view>

  <c:forEach begin="0" end="3"  var="i" varStatus="status">
    <c:set var="i" scope="request" value="${i}"/>
    <c:set var="status" scope="request" value="${status}"/>
    <c:set var="id" scope="request" value="foo${status.index}"/>
    Array[<c:out value="${i}"/>]: 
    <h:outputText id="#{id}" value="#{i}"/><br>
  </c:forEach>

</f:view>

</body>
</head>
</html>
