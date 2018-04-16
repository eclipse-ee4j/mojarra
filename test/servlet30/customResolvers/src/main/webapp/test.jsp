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
    <title>test</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  </head>

  <body>
    <h1>test</h1>

<f:view>

    <h:form prependId="false">

    <p>Invoking the variable resolver chain: <h:outputText value="#{requestBean.invokeVariableResolverThruChain}" />.</p>
    
    <p>Invoking the variable resolver directly: <h:outputText value="#{requestBean.invokeVariableResolverDirectly}" />.</p>

    <p>Invoking the EL resolver directly: <h:outputText value="#{requestBean.invokeELResolverDirectly}" />.</p>

    <p>result: <h:outputText value="#{requestScope['newERDirect']}" />.</p>

    <p>Invoking the EL resolver via chain: <h:outputText value="#{requestBean.invokeELResolverThruChain}" />.</p>

    <p>result: <h:outputText value="#{requestScope['newERThruChain']}" />.</p>

    <p><h:commandButton id="reload" value="reload" actionListener="#{requestBean.verifyELResolverChainIsCorrectlyConfigured}" /> </p>

    <p>StackTrace from el resolution: </p>
    <h:outputText escape="false" value="#{requestScope['message']}" />

  </h:form>

</f:view>

    <hr>
  </body>
</html>
