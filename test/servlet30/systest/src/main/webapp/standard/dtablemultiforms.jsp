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
    <title>dtablecolumnclasses.jsp</title>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

</head>
<body>
<f:view>
    Output: <h:outputText id="out" value="#{stringholder.string}"/>
    <h:dataTable id="table" value="#{listholder.list}" var="_dontcare">
        <h:column id="column">
            <h:form id="columnform">
                 Input: <h:inputText id="columninput" value="#{stringholder.string}"/>
                 <h:commandButton id="columnbutton" value="submit"/>
            </h:form>
        </h:column>
    </h:dataTable>
    <h:form id="finalform">
                 Input: <h:inputText id="finalinput" value="#{stringholder.string}"/>
                 <h:commandButton id="finalbutton" value="submit"/>
    </h:form>
</f:view>
</body>
</html>
