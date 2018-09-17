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

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>dtablecolumnclasses.jsp</title>
    <style type="text/css">
        .b1 {
            background-color: red;
        }

        .b2 {
            background-color: green;
        }
    </style>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>


    <%
        List<String[]> list = new ArrayList<String[]>(6);
        list.add(new String[]{"c1"});
        list.add(new String[]{"c1_1"});
        list.add(new String[]{"c1_2"});
        list.add(new String[]{"c1_3"});
        list.add(new String[]{"c1_4"});
        list.add(new String[]{"c1_5"});
        request.setAttribute("list", list);
    %>
</head>
<body>
<f:view>
    <h:dataTable value="#{requestScope.list}"
                 var="row"
                 rowClasses="b1,b2">
        <h:column rowHeader="true">
            <h:outputText value="#{row[0]}"/>
        </h:column>
    </h:dataTable>
</f:view>
</body>
</html>
