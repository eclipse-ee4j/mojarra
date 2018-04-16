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

<f:view>
<html>
<head>
<title>jstl-foreach-01</title>
</head>
<body>

<h:form id="jstlForeach01_form">

  <table border="0" cellspacing="5" cellpadding="5">

    <jsp:useBean scope="session" 
                 class="com.sun.faces.systest.model.ForEachBean"
                 id="forEachBean" />

    <c:forEach items="${forEachBean.arrayProperty}" varStatus="s" var="curVal">
      <c:set var="val" scope="request" value="${curVal}" />
      <c:set var="s" scope="request" value="${s}"/>
      <c:set var="arrayLabel" scope="request" value="arrayLabel${s.index}"/>
      <c:set var="arrayLabelValue" scope="request" 
             value="arrayLabel_${s.index}"/>
      <c:set var="arrayProp" scope="request" value="arrayProp${s.index}"/>
      <tr>
        <td><h:outputText id="#{arrayLabel}" value="#{arrayLabelValue}"/></td>
        <td><h:inputText id="#{arrayProp}" value="#{val}"/></td>
      </tr>
    </c:forEach>

    <tr>
      <td><h:commandButton id="submit" type="submit" value="Submit"/></td>
      <td><h:commandButton id="reset"  type="reset"  value="Reset"/></td>
    </tr>

  </table>

</h:form>
</body>
</html>
</f:view>
