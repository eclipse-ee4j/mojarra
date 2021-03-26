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

<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    java.util.ArrayList list = new java.util.ArrayList();
    list.add("one");
    list.add("two");
    list.add("three");
    pageContext.setAttribute("list", list, PageContext.REQUEST_SCOPE);
%>
<html>
  <head>
      <title>interweaving11</title>   
  </head>
  <body>
    <f:view>
        <h:form>
            <c:forEach items="#{list}" var="item">
                <h:commandLink>
                   <f:param name="param" value="value"/>
                   <h:outputText value="#{item}"/>
                </h:commandLink>
                </br>
            </c:forEach>
        </h:form>
    </f:view>
  </body>
</html>

