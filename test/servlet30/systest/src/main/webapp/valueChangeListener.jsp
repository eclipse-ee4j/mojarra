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
    <title>ValueChangeListeners and Validators</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
  </head>

  <body>
    <h1>ValueChangeListeners and Validators</h1>

<f:view>

  <h:form>

    <p>Enter numbers from 1 to 10.</p>

    <h:panelGrid columns="2">

      <h:inputText id="textA" 
	       valueChangeListener="#{valueChangeListenerBean.textAChanged}">
        <f:validateLongRange minimum="1" maximum="10" />
      </h:inputText>

      <h:inputText id="textB" 
	       valueChangeListener="#{valueChangeListenerBean.textBChanged}">
        <f:validateLongRange minimum="1" maximum="10" />
      </h:inputText>

      <h:outputText value="#{valueChangeListenerBean.textAResult}" />

      <h:outputText value="#{valueChangeListenerBean.textBResult}" />

      <h:commandButton value="submit" /> <p>

      <h:messages dir="LTR" lang="en"/>

      <hr />

      <h:message for="textB" dir="RTL" lang="de"/>


    </h:panelGrid>

  </h:form>

</f:view>

    <hr>
  </body>
</html>
