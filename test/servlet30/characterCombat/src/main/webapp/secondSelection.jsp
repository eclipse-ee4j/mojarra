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



<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<f:view>
<html>
<head>
  <title>
    Second Selection Page
  </title>
  <link rel="stylesheet" type="text/css"
    href='<%= request.getContextPath() + "/stylesheet.css" %>'>
</head>

<body>

  <h:graphicImage url="/images/header.jpg" />

  <h2>Second Selection Page</h2>

  <p>This page displays the same data as the previous page except it
     does not include the user's first combat choice so that character
     will not be picked twice.</p>

  <p>You may now choose your second character that will be waging a
  magical combat with <h:outputText value="#{modelBean.firstSelection}" />.</p>

  <h:form prependId="false">

    <h:panelGrid columns="1">

      <h:selectOneRadio
        layout="pageDirection" 
        required="true"
        value="#{modelBean.secondSelection}">
        <f:selectItems
          value="#{modelBean.charactersToSelect}" />
      </h:selectOneRadio>

      <h:messages />

    </h:panelGrid>

    <jsp:include page="wizard-buttons.jsp"/>


  </h:form>

</body>
</html>
</f:view>
