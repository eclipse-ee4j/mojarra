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
    <title>Show that createView uses overridden ViewRoot</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
    <%@ page import="javax.faces.context.FacesContext" %>
  </head>

  <body>
    <h1>Show that createView uses overridden ViewRoot</h1>

<% 
FacesContext context = FacesContext.getCurrentInstance();

context.getExternalContext().getRequestMap().put("root", context.getViewRoot().getClass().getName());
FacesContext.getCurrentInstance().getApplication().addComponent("javax.faces.ViewRoot", "javax.faces.component.UIViewRoot");
%>

<f:view>

<p>Replaced ViewRoot is <h:outputText value="#{requestScope.root}" /></p>

<p><a name="replace" href="replaceViewRoot.jsp">Go back to replace</a></p>

</f:view>

    <hr>
  </body>
</html>
