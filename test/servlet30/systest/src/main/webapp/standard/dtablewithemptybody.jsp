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

<%@ page import="java.util.ArrayList" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>dtablewithemptybody.jsp</title>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>


    <%
    	ArrayList<String> emptyList = new ArrayList<String>();
        request.setAttribute("emptyList", emptyList);
        ArrayList<String> someList = new ArrayList<String>();
        request.setAttribute("someList", someList);
        someList.add("AAAA");
        someList.add("BBBB");
        someList.add("CCCC");
    %>
</head>
<body>
<f:view>
    <h:dataTable value="#{requestScope.emptyList}" id="Empty"
                 var="row">
        <h:column rendered="false">
        	<h:outputText value="not rendered" />
        </h:column>
        <h:column>
        	<h:outputText value="#{row}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row}"/>
        </h:column>
    </h:dataTable>
    <h:dataTable value="#{requestScope.someList}" id="Some"
                 var="row">
        <f:facet name="header"><h:outputText value="Table Header" /></f:facet>
        <h:column rendered="false">
        	<f:facet name="header"><h:outputText value="Header1" /></f:facet>
        	<h:outputText value="not rendered" />
        </h:column>
        <h:column rendered="false">
        	<f:facet name="header"><h:outputText value="Header2" /></f:facet>
        	<h:outputText value="#{row}"/>
        </h:column>
        <h:column rendered="false">
        	<f:facet name="header"><h:outputText value="Header3" /></f:facet>
            <h:outputText value="#{row}"/>
        </h:column>
        <f:facet name="footer"><h:outputText value="Table Footer" /></f:facet>
    </h:dataTable>
    
    <h:dataTable id="PureEmptyDataTable">
    </h:dataTable>
    
    <h:panelGrid id="PureEmptyPanelGrid">
    </h:panelGrid>
    
    <h:panelGrid id="NoRenderedContentPanelGrid" columns="2">
    	<f:facet name="header"><h:outputText value="Header" /></f:facet>
    	<h:outputText value="AAA" rendered="false" />
    	<h:outputText value="BBB" rendered="false" />
    	<h:outputText value="CCC" rendered="false" />
    </h:panelGrid>
</f:view>
</body>
</html>
