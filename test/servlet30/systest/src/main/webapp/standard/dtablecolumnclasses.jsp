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

        .b3 {
            background-color: blue;
        }

        .b4 {
            background-color: burlywood;
        }
        .b5 {
            background-color: darkolivegreen;
        }
        .b6 {
            background-color: darkviolet;
        }
        .b7 {
            background-color: skyblue;
        }
    </style>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>


    <%
        List<String[]> list = new ArrayList<String[]>(1);
        list.add(new String[]{"c1", "c2", "c3", "c4", "c5", "c6"});
        list.add(new String[]{"c1_1", "c2_1", "c3_1", "c4_1", "c5_1", "c6_1"});
        request.setAttribute("list", list);
    %>
</head>
<body>
<f:view>
    <h:dataTable value="#{requestScope.list}"
                 var="row"
                 columnClasses="b1,b2">
        <h:column>
            <h:outputText value="#{row[0]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[1]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[2]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[3]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[4]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[5]}"/>
        </h:column>
    </h:dataTable>
    <h:dataTable value="#{requestScope.list}"
                 var="row"
                 columnClasses="b1,b2,b3,b4,">
        <h:column>
            <h:outputText value="#{row[0]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[1]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[2]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[3]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[4]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[5]}"/>
        </h:column>
    </h:dataTable>
    <h:dataTable value="#{requestScope.list}"
                 var="row"
                 columnClasses="b1,b2,b3">
        <h:column>
            <h:outputText value="#{row[0]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[1]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[2]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[3]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[4]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[5]}"/>
        </h:column>
    </h:dataTable>
    <h:dataTable value="#{requestScope.list}"
                 var="row"
                 columnClasses="b1">
        <h:column>
            <h:outputText value="#{row[0]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[1]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[2]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[3]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[4]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[5]}"/>
        </h:column>
    </h:dataTable>
    <h:dataTable value="#{requestScope.list}"
                 var="row">
        <h:column>
            <h:outputText value="#{row[0]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[1]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[2]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[3]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[4]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[5]}"/>
        </h:column>
    </h:dataTable>
     <h:dataTable value="#{requestScope.list}"
                 var="row"
                 columnClasses="b1,b2,b3,b4,b5,b6,b7">
        <h:column>
            <h:outputText value="#{row[0]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[1]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[2]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[3]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[4]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[5]}"/>
        </h:column>
    </h:dataTable>
    <h:dataTable value="#{requestScope.list}"
                 var="row"
                 columnClasses="b1,b2,b3,b4,">
        <h:column>
            <h:outputText value="#{row[0]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[1]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[2]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[3]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[4]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[5]}"/>
        </h:column>
    </h:dataTable>
<h:dataTable value="#{requestScope.list}"
                 var="row"
                 columnClasses=",b2,,,b4,b5,b6">
        <h:column>
            <h:outputText value="#{row[0]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[1]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[2]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[3]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[4]}"/>
        </h:column>
        <h:column>
            <h:outputText value="#{row[5]}"/>
        </h:column>
    </h:dataTable>
</f:view>
</body>
</html>


