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

<%@ page contentType="text/html" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<%@ page import="java.util.*"%>

<%
        for (int j = 0; j < 3; ++j)
        {
            String mapName = "input" + (j + 1);
            if (pageContext.getAttribute(mapName) == null)
            {
                Map map = new LinkedHashMap();
                for (int i = 0; i < 3; ++i)
                    map.put("inputText" + (j * 3 + i + 1), "input" + (j * 3 + i + 1));
                pageContext.setAttribute(mapName, map, pageContext.SESSION_SCOPE);
            }
        }
%>
<f:view>
    <html>
    <head>
    <title>Test Interaction of c:forEach, h:label and h:message</title>
    <style type="text/css">
    label { font-family: sans-serif; font-weight: bold; font-size: 0.8em; }
    .message { color: red; font-weight: bold; }
    .subheading { font-weight: bold; background-color: #cfffcf; }
    </style>
    </head>
    <body>

    <br>
    <h:form id="myform">
        <h:panelGrid>
            <f:facet name="header">
                <h:outputText
                    value="Test Interaction of c:forEach, h:label and h:message" />
            </f:facet>

            <!-- list all messages -->
            <h:messages id="messages" layout="table" styleClass="message" />

            <!-- label without "id" -->
            <h:outputText styleClass="subheading"
                value="Test simple label and inputText and no id on the label" />
            <h:outputLabel for="inputInt1" value="Label for intProperty below" />
            <h:message for="inputInt1" styleClass="message" />
            <h:inputText id="inputInt1" value="#{forEachBean1.intProperty}"
                required="true" />

            <h:inputText id="inputByte1" value="#{forEachBean1.byteProperty}"
                required="true" />
            <h:message for="inputByte1" styleClass="message" />
            <h:outputLabel for="inputByte1" value="Label for byteProperty above" />

            <h:outputText styleClass="subheading"
                value="Test c:ForEach with label and inputText and no id on the label" />
            <c:forEach var="item" items="#{input1}">
                <h:outputLabel for="inputId1" value="Label for #{item.key} below" />
                <h:message for="inputId1" styleClass="message" />
                <h:inputText id="inputId1" value="#{input1[item.key]}" required="true" />
            </c:forEach>

            <!-- label with "id" -->
            <h:outputText styleClass="subheading"
                value="Test simple label and inputText with an id on the label" />
            <h:outputLabel id="inputLong1Label" for="inputLong1"
                value="Label for longProperty below" />
            <h:message id="inputLong1Msg" for="inputLong1" styleClass="message" />
            <h:inputText id="inputLong1" value="#{forEachBean1.longProperty}"
                required="true" />

            <h:inputText id="inputShort1" value="#{forEachBean1.longProperty}"
                required="true" />
            <h:message id="inputShort1Msg" for="inputShort1" styleClass="message" />
            <h:outputLabel id="inputShort1Label" for="inputShort1"
                value="Label for shortProperty above" />

            <h:outputText styleClass="subheading"
                value="Test c:ForEach with label and inputText with an id on the label" />
            <c:forEach var="item" items="#{input2}">
                <h:inputText id="inputId2" value="#{input2[item.key]}" required="true" />
                <h:message id="inputId2Msg" for="inputId2" styleClass="message" />
                <h:outputLabel id="inputId2Label" for="inputId2"
                    value="Label for #{item.key} above" />
            </c:forEach>

            <h:outputText styleClass="subheading"
                value="Test c:ForEach with transposed table" />
            <h:panelGroup>
                <h:panelGrid columns="#{fn:length(input3)}">
                    <c:forEach var="item" items="#{input3}">
                        <h:outputLabel for="inputId3" value="Label for #{item.key} below" />
                    </c:forEach>
                    <c:forEach var="item" items="#{input3}">
                        <h:message for="inputId3" styleClass="message" />
                    </c:forEach>
                    <c:forEach var="item" items="#{input3}">
                        <h:inputText id="inputId3" value="#{input3[item.key]}"
                            required="true" />
                    </c:forEach>
                </h:panelGrid>
            </h:panelGroup>
        </h:panelGrid>
        
        <jsp:include page="forEach03Include.jsp" />       

        <h:commandButton id="submit" value="Submit" />
    </h:form>
    </body>
    </html>
</f:view>
