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
    <title>pgridcolumnclasses.jsp</title>
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

</head>
<body>
<f:view>
<h:panelGrid columns="6"
             columnClasses="b1,b2">
    <h:outputText value="c1"/>
    <h:outputText value="c2"/>
    <h:outputText value="c3"/>
    <h:outputText value="c4"/>
    <h:outputText value="c5"/>
    <h:outputText value="c6"/>

    <h:outputText value="c1_1"/>
    <h:outputText value="c2_1"/>
    <h:outputText value="c3_1"/>
    <h:outputText value="c4_1"/>
    <h:outputText value="c5_1"/>
    <h:outputText value="c6_1"/>
</h:panelGrid>
<h:panelGrid columns="6"
             columnClasses="b1,b2,b3,b4,">
    <h:outputText value="c1"/>
    <h:outputText value="c2"/>
    <h:outputText value="c3"/>
    <h:outputText value="c4"/>
    <h:outputText value="c5"/>
    <h:outputText value="c6"/>

    <h:outputText value="c1_1"/>
    <h:outputText value="c2_1"/>
    <h:outputText value="c3_1"/>
    <h:outputText value="c4_1"/>
    <h:outputText value="c5_1"/>
    <h:outputText value="c6_1"/>
</h:panelGrid>
<h:panelGrid columns="6"
             columnClasses="b1,b2,b3">
    <h:outputText value="c1"/>
    <h:outputText value="c2"/>
    <h:outputText value="c3"/>
    <h:outputText value="c4"/>
    <h:outputText value="c5"/>
    <h:outputText value="c6"/>

    <h:outputText value="c1_1"/>
    <h:outputText value="c2_1"/>
    <h:outputText value="c3_1"/>
    <h:outputText value="c4_1"/>
    <h:outputText value="c5_1"/>
    <h:outputText value="c6_1"/>
</h:panelGrid>
<h:panelGrid columns="6"
             columnClasses="b1">
    <h:outputText value="c1"/>
    <h:outputText value="c2"/>
    <h:outputText value="c3"/>
    <h:outputText value="c4"/>
    <h:outputText value="c5"/>
    <h:outputText value="c6"/>

    <h:outputText value="c1_1"/>
    <h:outputText value="c2_1"/>
    <h:outputText value="c3_1"/>
    <h:outputText value="c4_1"/>
    <h:outputText value="c5_1"/>
    <h:outputText value="c6_1"/>
</h:panelGrid>
<h:panelGrid columns="6">
    <h:outputText value="c1"/>
    <h:outputText value="c2"/>
    <h:outputText value="c3"/>
    <h:outputText value="c4"/>
    <h:outputText value="c5"/>
    <h:outputText value="c6"/>

    <h:outputText value="c1_1"/>
    <h:outputText value="c2_1"/>
    <h:outputText value="c3_1"/>
    <h:outputText value="c4_1"/>
    <h:outputText value="c5_1"/>
    <h:outputText value="c6_1"/>
</h:panelGrid>
<h:panelGrid columns="6"
             columnClasses="b1,b2,b3,b4,b5,b6,b7">
    <h:outputText value="c1"/>
    <h:outputText value="c2"/>
    <h:outputText value="c3"/>
    <h:outputText value="c4"/>
    <h:outputText value="c5"/>
    <h:outputText value="c6"/>

    <h:outputText value="c1_1"/>
    <h:outputText value="c2_1"/>
    <h:outputText value="c3_1"/>
    <h:outputText value="c4_1"/>
    <h:outputText value="c5_1"/>
    <h:outputText value="c6_1"/>
</h:panelGrid>
<h:panelGrid columns="6"
             columnClasses=",b2,,,b5,b6,b7">
    <h:outputText value="c1"/>
    <h:outputText value="c2"/>
    <h:outputText value="c3"/>
    <h:outputText value="c4"/>
    <h:outputText value="c5"/>
    <h:outputText value="c6"/>

    <h:outputText value="c1_1"/>
    <h:outputText value="c2_1"/>
    <h:outputText value="c3_1"/>
    <h:outputText value="c4_1"/>
    <h:outputText value="c5_1"/>
    <h:outputText value="c6_1"/>
</h:panelGrid>
</f:view>
</body>
</html>


