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

<HTML>
    <HEAD> <TITLE> JSF Basic Components Test Page </TITLE> </HEAD>

    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

    <BODY>
        <H3> JSF Basic Components Test Page </H3>


       <f:view>
        <h:form id="/basicForm">

            <h:textentry_input id="userName" />

            <h:commandButton id="login" />

	    <!-- <h:commandLink id="/basicForm/login" /> -->

            <h:outputText id="userLabel" />

            <h:selectManyCheckbox id="validUser" />

            <h:selectOneListbox id="appleQuantity" />

            <h:selectOneRadio id="shipType" />

            <h:textentry_secret id="password" />

            <h:textentry_textarea id="address" />


        </h:form>
       </f:view>

    </BODY>
</HTML>
