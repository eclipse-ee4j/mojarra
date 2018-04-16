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

<html>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<f:view>
<head>
<title><h:outputText id="title" value="title"/></title>
</head>

<body>
<h:form id="form">
   <h:panelGrid id="panel1" columns="2" styleClass="book"
      columnClasses="menuColumn, chapterColumn">

      <f:verbatim >
         verbatim one text here
      </f:verbatim>

      <h:panelGrid id="panel2" columns="1" >
         <h:outputText id="outputheader" value="this is the header" />
         <f:verbatim><hr/></f:verbatim>
      </h:panelGrid>

      <h:commandButton id="submit" value="submit"/>

      <f:verbatim >
         verbatim two text here
      </f:verbatim>

   </h:panelGrid>
</h:form>
</body>
</f:view>
</html>
