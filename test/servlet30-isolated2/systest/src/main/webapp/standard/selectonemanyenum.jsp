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

<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<html>
    <head>
        <title>SelectOneMany Enum Test</title>
    </head>
</html>
<body>
<f:view>
    <h:form id="test">
        <p>
        <h:selectOneMenu id="selected" value="#{enumbean.selected}">
            <f:selectItem itemValue="Value1" itemLabel="Value1"/>
            <f:selectItem itemValue="Value2" itemLabel="Value2"/>
            <f:selectItem itemValue="Value3" itemLabel="Value3"/>
            <f:selectItem itemValue="Value4" itemLabel="Value4"/>
        </h:selectOneMenu>
        </p>
        <p>
        <h:selectOneListbox id="selected2" value="#{enumbean.selected2}">
            <f:selectItem itemValue="Value1" itemLabel="Value1"/>
            <f:selectItem itemValue="Value2" itemLabel="Value2"/>
            <f:selectItem itemValue="Value3" itemLabel="Value3"/>
            <f:selectItem itemValue="Value4" itemLabel="Value4"/>
        </h:selectOneListbox>
        </p>
        <p>
        <h:selectOneMenu id="selected3" value="#{enumbean.selected3}">
            <f:selectItem itemValue="Value1" itemLabel="Value1"/>
            <f:selectItem itemValue="Value2" itemLabel="Value2"/>
            <f:selectItem itemValue="Value3" itemLabel="Value3"/>
            <f:selectItem itemValue="Value4" itemLabel="Value4"/>
        </h:selectOneMenu>
        </p>
        <p>
        <h:selectManyListbox id="array" value="#{enumbean.selectedArray}">
            <f:selectItem itemValue="Value1" itemLabel="Value1"/>
            <f:selectItem itemValue="Value2" itemLabel="Value2"/>
            <f:selectItem itemValue="Value3" itemLabel="Value3"/>
            <f:selectItem itemValue="Value4" itemLabel="Value4"/>
        </h:selectManyListbox>
        </p>
        <p>
        <h:selectManyListbox id="list" value="#{enumbean.selectedList}">
            <f:selectItem itemValue="Value1" itemLabel="Value1"/>
            <f:selectItem itemValue="Value2" itemLabel="Value2"/>
            <f:selectItem itemValue="Value3" itemLabel="Value3"/>
            <f:selectItem itemValue="Value4" itemLabel="Value4"/>
        </h:selectManyListbox>
        </p>
        <h:commandButton value="Submit"/>
    </h:form>
</f:view>
</body>
</html>
