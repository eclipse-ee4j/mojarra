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

<f:view>
<html>
<head>
<title>f:setPropertyActionListener</title>
</head>
<body>
<h:form>

Integer Property is: <h:outputText value="#{test3.intProperty}" />. <br>
String Property is: <h:outputText value="#{test3.stringProperty}" />. <br>

<h:commandButton id="expressionButton1" value="Convert from String To Integer">
  <f:setPropertyActionListener target="#{test3.intProperty}" value="100" />
</h:commandButton>
<h:commandButton id="expressionButton2" value="Convert from Integer to String">
  <f:setPropertyActionListener target="#{test3.stringProperty}" value="#{test3.intProperty}" />
</h:commandButton>   
<h:commandButton id="expressionButton3" value="String to String">
  <f:setPropertyActionListener target="#{test3.stringProperty}" value="String" />
</h:commandButton>
<h:commandButton id="expressionButton4" value="FacesContext to String">
  <f:setPropertyActionListener target="#{test3.stringProperty}" value="#{facesContext}" />
</h:commandButton>

</h:form>
</body>
</html>
</f:view>
