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
<title>form-input-02</title>
</head>
<body>

<h:form id="formInput02_form">

  <h:panelGrid columns="3">

    <h:outputText value="booleanProperty"/>
    <h:inputText id="booleanProperty" value="#{formInput02.booleanProperty}"/>
    <h:message for="booleanProperty"/>

    <h:outputText value="byteProperty"/>
    <h:inputText id="byteProperty" value="#{formInput02.byteProperty}"/>
    <h:message for="byteProperty"/>

    <h:outputText value="doubleProperty"/>
    <h:inputText id="doubleProperty" value="#{formInput02.doubleProperty}"/>
    <h:message for="doubleProperty"/>

    <h:outputText value="floatProperty"/>
    <h:inputText id="floatProperty" value="#{formInput02.floatProperty}"/>
    <h:message for="floatProperty"/>

    <h:outputText value="intProperty"/>
    <h:inputText id="intProperty" value="#{formInput02.intProperty}"/>
    <h:message for="intProperty"/>

    <h:outputText value="longProperty"/>
    <h:inputText id="longProperty" value="#{formInput02.longProperty}"/>
    <h:message for="longProperty"/>

    <h:outputText value="shortProperty"/>
    <h:inputText id="shortProperty" value="#{formInput02.shortProperty}"/>
    <h:message for="shortProperty"/>

    <h:outputText value="stringProperty"/>
    <h:inputText id="stringProperty" value="#{formInput02.stringProperty}"/>
    <h:message for="stringProperty"/>

    <h:commandButton id="submit" type="submit" value="Submit"/>
    <h:commandButton id="reset"  type="reset"  value="Reset"/>
    <h:outputText value=""/>

  </h:panelGrid>

</h:form>

</body>
</html>
</f:view>
