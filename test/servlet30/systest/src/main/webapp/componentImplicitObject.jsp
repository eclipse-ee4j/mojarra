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
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%
    java.util.List<String> l = new java.util.ArrayList<String>(2);
    l.add("one");
    l.add("two");
    request.setAttribute("list", l);
%>
<f:view>
    <h:form prependId="false">
        <h:outputText id="ot" value="#{component.id}"/>
        <h:dataTable value="#{requestScope.list}" var="v">
            <h:column>
                <f:facet name="header" >
                    <h:outputText id="facetOT" value="#{component.id}"/>
                </f:facet>
                <h:inputText id="it" value="#{component.id}"/>
            </h:column>
        </h:dataTable>        
    </h:form>
</f:view>
