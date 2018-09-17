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
<%@ taglib prefix="s" uri="/WEB-INF/taglib.tld" %>

<f:view>
<html>
<head>
<title>jstl-if-05</title>
</head>
<body>
[1]
<c:if test="${param.cond}">
  <h:outputText       id="other2"  value="[2]"/>
  [3]
  <s:children id="kids">
    [4a]
    <h:outputText     id="kids4b" value="[4b]"/>
    [4c]
  </s:children>
  <h:outputText       id="other5" value="[5]"/>
  [6]
</c:if>
[7]
</body>
</html>
</f:view>
