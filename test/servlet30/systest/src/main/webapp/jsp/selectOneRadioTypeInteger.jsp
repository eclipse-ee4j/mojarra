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

<html>
<body>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<jsp:useBean id="myBean" class="com.sun.faces.systest.model.TestBean" scope="session" />

<f:view>
    <h:form id="form">
        <h:selectOneRadio converter="javax.faces.Integer"
           value="#{myBean.selectedValue}">
            <f:selectItems value="#{myBean.mySelectItems}"/>
        </h:selectOneRadio>
        <h:outputText value="Model Selection:"/>
        <h:outputText value="#{myBean.selectedValue}"/>
        <br>
        <h:commandButton id="nonImmediate" value="Submit immedate false" action="success"/>
        <h:commandButton id="immediate" value="Submit immediate true" immediate="true" action="success"/>
    </h:form>
    <h:messages/>
</f:view>

</body>
</html>
