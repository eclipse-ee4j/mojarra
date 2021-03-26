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
    <title>Validators</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
  </head>

  <body>
    <h1>Validators</h1>

<f:view>

  <h:form>

    <h:panelGrid columns="3">

<%-- Case 1: Double Range Validator with no "label" attribute --%>

      <h:outputText value="DoubleRange1:"/>
      <h:inputText id="dr1"> 
        <f:validateDoubleRange minimum="2" maximum="5" />
      </h:inputText>
      <h:message for="dr1" showSummary="true" />

<%-- Case 2: Double Range Validator with "label" attribute --%>

      <h:outputText value="DoubleRange2:"/>
      <h:inputText id="dr2" label="DoubleRange2"> 
        <f:validateDoubleRange minimum="2" maximum="5" />
      </h:inputText>
      <h:message for="dr2" showSummary="true" />

<%-- Case 3: Length Validator with no "label" attribute --%>

      <h:outputText value="Length1:"/>
      <h:inputText id="l1"> 
        <f:validateLength minimum="2" maximum="5" />
      </h:inputText>
      <h:message for="l1" showSummary="true" />

<%-- Case 4: Length Validator with "label" attribute --%>

      <h:outputText value="Length2:"/>
      <h:inputText id="l2" label="Length2"> 
        <f:validateLength minimum="2" maximum="5" />
      </h:inputText>
      <h:message for="l2" showSummary="true" />

<%-- Case 5: Long Range Validator with no "label" attribute --%>

      <h:outputText value="LongRange1:"/>
      <h:inputText id="lr1"> 
        <f:validateLongRange minimum="2" maximum="5" />
      </h:inputText>
      <h:message for="lr1" showSummary="true" />

<%-- Case 6: Long Range Validator with "label" attribute --%>

      <h:outputText value="LongRange2:"/>
      <h:inputText id="lr2" label="LongRange2"> 
        <f:validateLongRange minimum="2" maximum="5" />
      </h:inputText>
      <h:message for="lr2" showSummary="true" />

      <h:commandButton value="submit" />

    </h:panelGrid>

  </h:form>

</f:view>

    <hr>
  </body>
</html>
