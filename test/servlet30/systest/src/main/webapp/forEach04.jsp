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

<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String[] paths = new String[] { "frag1", "frag2" };
    request.setAttribute("paths", paths);
%>
<f:view>
    <html>
    <head>
        <title>forEach04.jsp (validates fix for Issue 714)</title>
    </head>
    <body>
    <h:form>
        <h:commandButton id="clickit" value="Click Me"/>
        <c:forEach items="#{paths}" var="fragmentPath">
            <f:subview id="id${fragmentPath}">
                <jsp:include page="/${fragmentPath}.jsp"/>
            </f:subview>
        </c:forEach>
    </h:form>
    </body>
    </html>
</f:view>
