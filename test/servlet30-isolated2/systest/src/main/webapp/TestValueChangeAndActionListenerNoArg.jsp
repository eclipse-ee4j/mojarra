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
    <title>Test that method expressions pointing to no-arg methods work for valueChangeListener and actionListener</title>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  </head>

  <body>
    <h1>Test that method expressions pointing to no-arg methods work for valueChangeListener and actionListener</h1>


<f:view>
    <h:form id="form" prependId="false">

      <p>

         <h:inputText id="username" value="#{test1.stringProperty}" 
                required="true"
             valueChangeListener="#{test1.valueChange0}"/>

      </p>

      <p>

	<h:commandButton id="loginEvent" value="Login" 
             actionListener="#{test1.actionListener0}">

	</h:commandButton>  

      </p>

<p>valueChange0Called: <h:outputText value="#{valueChange0Called}" /></p>

<p>actionListener0Called: <h:outputText value="#{actionListener0Called}" /></p>

    </h:form>
</f:view>

  </body>
</html>
