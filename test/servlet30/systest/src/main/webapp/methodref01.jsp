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
    <title>Test Method References</title>
  </head>

  <body>
    <h1>Test Method References</h1>

    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    <c:set scope="application" var="jakarta.faces.VALIDATE_EMPTY_FIELDS"
           value="false" />

    <f:view>  
      <h:form id="form">

	<hr>
	<p>Press a button, see some text.</p>
        <h:inputText readonly="true" id="buttonStatus" 
                      value="#{methodRef.buttonPressedOutcome}"/>
        <h:commandButton id="button1" value="button1"
                           action="#{methodRef.button1Pressed}"/>
        <h:commandLink id="button2" action="#{methodRef.button2Pressed}">
          <h:outputText value="button2"/>
        </h:commandLink>
        <h:commandButton id="button3" value="button3"
                           actionListener="#{methodRef.button3Pressed}"/>
                            <h:message for="buttonStatus"/>
        <hr>
	<p>the only valid value is batman</p>
        <h:inputText id="toValidate" 
                      validator="#{methodRef.validateInput}"/>
        <h:commandButton id="validate" value="validate"/>
        <h:message for="toValidate"/>

        <hr>
	<p>test value change</p>
        <h:inputText id="toChange" 
                      valueChangeListener="#{methodRef.valueChange}"/>
        <h:commandButton id="changeValue" value="changeValue"/>
        <h:message for="toChange"/>
      </h:form>
    </f:view>



    <hr>
    <address><a href="mailto:Ed Burns <ed.burns@sun.com>"></a></address>
<!-- Created: Fri Oct 31 10:49:23 Eastern Standard Time 2003 -->
<!-- hhmts start -->
Last modified: Thu Feb 26 13:28:40 EST 2009
<!-- hhmts end -->
  </body>
</html>
