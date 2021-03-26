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
    Combat Results Page
  </title>
  <link rel="stylesheet" type="text/css"
    href='<%= request.getContextPath() + "/stylesheet.css" %>'>
</head>

<body>

  <h:graphicImage url="/images/header.jpg" />

  <h2>Combat Results Page</h2>

  <p>The results page determines the winner of the two selections
     based on a magically simple algorithm</p>

  <p>If 
     <i><h:outputText value="#{modelBean.firstSelection}" /></i>
     and 
     <i><h:outputText value="#{modelBean.secondSelection}" /></i>
     were to wage a magical war, the winner would be: 
     <b><h:outputText value="#{modelBean.combatWinner}" /></b>
     !
  </p>

  <h:form>
    <h:commandButton value="Start Over" action="startOver" />
    <jsp:include page="wizard-buttons.jsp"/>
  </h:form>


</body>
</html>
</f:view>
