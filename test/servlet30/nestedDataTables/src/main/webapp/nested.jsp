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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:view>
    <html>
        <head>
            <title>Nested Tables</title>
        </head>
        <body>
            <h:form id="form">
                <h:dataTable id="outer" value="#{testbean.services}" var="service">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Service"/>
                        </f:facet>
                        <h:inputText id="inputText" value="#{service.name}" required="true"/>
                        <h:commandButton id="delete" styleClass="command-multiple" immediate="false" action="#{testbean.deleteService}" value="Delete"/>
                        <f:facet name="footer">
                            <h:commandButton id="add" styleClass="command-multiple" immediate="false" action="#{testbean.addService}" value="Add"/>
                        </f:facet>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Port"/>
                        </f:facet>
                        <h:dataTable id="inner" value="#{service.ports}" var="portNumber">
                            <h:column>
                                <h:inputText id="portNumber" value="#{portNumber.portNumber}" size="5"/>
                                <h:commandButton styleClass="command-multiple" immediate="false" action="#{testbean.deletePortNumber}" value="Delete"/>
                                <f:facet name="footer">
                                    <h:commandButton id="add-port" styleClass="command-multiple" immediate="false"  action="#{testbean.addPortNumber}" value="Add port"/>
                                </f:facet>
                            </h:column>
                        </h:dataTable>
                    </h:column>
                </h:dataTable>  
                <hr />
                <h:commandButton id="reload" value="reload" action="#{testbean.printTree}"/>
                <p>Current state after previous load: <h:outputText escape="false" value="#{testbean.currentStateTable}" /></p>
            </h:form>
        </body>
    </html>
</f:view>
