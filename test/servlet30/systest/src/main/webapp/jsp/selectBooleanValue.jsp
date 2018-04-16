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
    <title>selectBoolean test</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
  </head>

  <body>
    <h1>selectBoolean test</h1>

<h2>How this testcase works.</h2>

<p>This is the regression test for bugtraq 5016123.</p>

<p>The system test for this page does the following</p>


	<ol>

	  <li><p>presses the button with the id "replace" twice.</p>

          <p>The first time pressed, it replaces the default
          PropertyResolver with one that logs calls to setValue in the
          "valueChanged" property of bean named test3.  The second time
          pressed, you'll actually see that the setValue was called.</p>

          </li>

	  <li><p>When the page loads from the second button press, it
	  looks for the string "setValue() called" and verifies it is
	  not in the page. </p></li>

	  <li><p>presses the button with the id "restore".
	  </p></li>

	</ol>


<f:view>

  <h:form>

    <h:commandButton id="replace" value="submit and replace PropertyResolver with Logging PropertyResolver" 
                     actionListener="#{test3.replacePropertyResolver}" />

    <h:panelGrid columns="2">

      <h:selectBooleanCheckbox value="#{test3.booleanProperty2}" />

      <h:outputText value="checkbox" />

      <h:outputText value="valueChanged:" />

      <h:outputText value="#{test3.valueChangeMessage}" />

      <h:messages />

    </h:panelGrid>

    <h:commandButton id="restore" 
                     value="submit and restore original PropertyResolver" 
                     actionListener="#{test3.restorePropertyResolver}" />


  </h:form>

</f:view>

    <hr>
  </body>
</html>
