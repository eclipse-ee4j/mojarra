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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Converters</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
    <%@ taglib uri="/WEB-INF/taglib.tld"           prefix="s" %>
  </head>

  <body>
    <h1>Converters</h1>

<f:loadBundle basename="com.sun.faces.CustomMessages" var="customBundle"/>

<f:view>

  <h:form id="form">

    <h:panelGrid id="panelGrid" columns="3">


<%--
      Exercises jakarta.faces.webapp.ConverterELTag when ConverterException
      Expected result: FacesMessage queued;  Log message;
--%>
      <h:outputText value="Number4:" />
      <h:inputText id="number4" label="Number4" size="10" maxlength="20" value="aaa">
         <f:convertNumber type="number" />
      </h:inputText>
      <h:message for="number4" showSummary="true" />

<%--
      Exercises jakarta.faces.webapp.ConverterELTag when ConverterException
      Expected result: Log message;
--%>
      <h:outputText value="Number5:" />
      <h:outputText id="number5" value="aaa">
         <f:convertNumber type="number" />
      </h:outputText>
      <h:message for="number5" showSummary="true" />

<%--
      Exercises jakarta.faces.webapp.ConverterELTag when ConverterException
      Expected result: FacesMessage queued;  Log message; 
--%>
      <h:outputText value="Number6:" />
      <h:inputText id="number6" label="Number6" size="10" maxlength="20" value="aaa" converterMessage="My own message">
         <f:convertNumber type="number" />
      </h:inputText>
      <h:message for="number6" showSummary="false" />

<%--
      Exercises jakarta.faces.webapp.ConverterTag when ConverterException
      Expected result: FacesMessage queued;  Log message; 
--%>
      <h:outputText value="Number6:" />
      <h:outputText value="Number7:" />
      <h:inputText id="number7" label="Number7" size="10" maxlength="20" value="aaa">
         <s:converter converterId="jakarta.faces.Number" />
      </h:inputText>
      <h:message for="number7" showSummary="true" />

<%--
      Exercises jakarta.faces.webapp.ConverterTag when ConverterException
      Expected result: Log message;
--%>
      <h:outputText value="Number8:" />
      <h:outputText id="number8" value="aaa">
         <s:converter converterId="jakarta.faces.Number" />
      </h:outputText>
      <h:message for="number8" showSummary="true" />

<%--
      Exercises jakarta.faces.webapp.ConverterTag when ConverterException
      Expected result: FacesMessage queued;  Log message; 
--%>
      <h:outputText value="Number9:" />
      <h:inputText id="number9" label="Number6" size="10" maxlength="20" value="aaa" converterMessage="My own message">
         <s:converter converterId="jakarta.faces.Number" />
      </h:inputText>
      <h:message for="number9" showSummary="false" />
      <h:commandButton value="submit" /> 
    </h:panelGrid>

  </h:form>

</f:view>

    <hr>
  </body>
</html>
