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
<title>immediate01</title>
</head>
<body>

<h:form>

<p>PENDING: write an HTMLUNIT testcase for this.</p>

Output: <h:outputText value="#{immediateBean.stringProperty}"/> Input: 
<h:inputText value="#{immediateBean.stringProperty}"/> <br />
<h:commandLink action="null">Submit</h:commandLink> <p />
<h:commandLink action="null" immediate="true">Cancel</h:commandLink> <p />

<h:commandButton value="Submit" action="null" /><p />
<h:commandButton value="Cancel" action="null" immediate="true" /><p />

<hr />

<h:commandButton value="Clear Bean Property">
  <f:setPropertyActionListener target="#{immediateBean.stringProperty}" value="" />
</h:commandButton>

</h:form>

</body>
</html>
</f:view>
