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
  </head>

  <body>
    <h1>Converters</h1>

<f:view>

  <h:form>

    <h:panelGrid columns="2">

<%-- Case 1: Custom Converter with "converterId" attribute --%>

      <h:inputText id="text1"> 
        <f:converter converterId="TestConverter01" />
      </h:inputText>

      <h:message for="text1" />

<%-- Case 2: Custom Converter with "binding" attribute --%>

      <h:inputText id="text2"> 
        <f:converter binding="#{converterBean.converter}" />
      </h:inputText>

      <h:message for="text2" />

<%-- Case 3: "converterId" and "binding" specified                        --%>
<%--         "binding" will set the instance (created from "converterId") --%>
<%--         to a property on the backing bean                     --%>

      <h:inputText id="text3"> 
        <f:converter converterId="TestConverter01"
           binding="#{converterBean.converter}" />
      </h:inputText>

      <h:message for="text3" />

<%-- Bind the converter we created (Case 3) to the component --%>

      <h:inputText id="text4">
        <f:converter binding="#{converterBean.converter}" />
      </h:inputText>

      <h:message for="text4" />

<%-- DateTime Converter with "binding" attribute --%>

      <h:inputText id="text5" label="text5" value="10:00:01 PM" size="10" maxlength="20">
         <f:convertDateTime binding="#{converterBean.dateTimeConverter}"
            type="time" timeStyle="medium"/>
      </h:inputText>

      <h:message for="text5" />

<%-- Case 1: Double Converter with "converterId" attribute --%>

      <h:inputText id="text6" value="100" size="10" maxlength="20">
         <f:converter converterId="jakarta.faces.Double" />
      </h:inputText>

      <h:message for="text6" />

<%-- Case 2: Double Converter with "binding" attribute --%>

      <h:inputText id="text7" value="100" size="10" maxlength="20">
         <f:converter binding="#{converterBean.doubleConverter}" />
      </h:inputText>

      <h:message for="text7" />

<%-- Case 3: Double Converter "converterId" and "binding" specified       --%> 
<%--         "binding" will set the instance (created from "converterId") --%>
<%--         to a property on the backing bean                            --%>

      <h:inputText id="text8"> 
        <f:converter converterId="jakarta.faces.Double"
           binding="#{converterBean.doubleConverter}" />
      </h:inputText>

      <h:message for="text8" />

<%-- Number Converter with "binding" attribute --%>

      <h:inputText id="text9" value="9" size="10" maxlength="20">
         <f:convertNumber binding="#{converterBean.numberConverter}" />
      </h:inputText>

      <h:message for="text9" />

      <h:commandButton value="submit" /> <h:messages />

    </h:panelGrid>

  </h:form>

</f:view>

    <hr>
  </body>
</html>
