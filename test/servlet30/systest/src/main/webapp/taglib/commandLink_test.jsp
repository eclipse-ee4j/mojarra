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
<head>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <title>commandLink_test.jsp</title>
</head>
<body>
    <f:loadBundle basename="com.sun.faces.systest.resources.Resources" 
         var="messageResources"/>
    <f:view>
      <h:form id="form01">
        <h:commandLink id="hyperlink01"><f:verbatim>My Link</f:verbatim></h:commandLink>
        <h:commandLink id="hyperlink02"><h:outputText value="#{test1.stringProperty}"/></h:commandLink>
        <h:commandLink id="hyperlink03"><h:outputText value="#{messageResources.hyperlink_key}"/></h:commandLink>
        <h:commandLink id="hyperlink04"><f:verbatim escape="false"><img src="duke.gif" /></f:verbatim></h:commandLink>
        <h:commandLink id="hyperlink05"><h:graphicImage value="#{messageResources.image_key}"/></h:commandLink>
        <h:commandLink id="hyperlink06"><f:verbatim>Paramter Link</f:verbatim>
            <f:param name="param1" value="value1"/>
        </h:commandLink>
      </h:form>
    </f:view>
</body>
</html>

