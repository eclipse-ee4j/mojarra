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

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@page import="javax.faces.component.*,javax.faces.context.*" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ProcessSaveRestoreStateTest</title>
    </head>
    <body>
    <%
       UIComponent comp1 = new UIOutput();
       UIComponent child11 = new UIInput();
       child11.setTransient(true);
       UIComponent child12 = new UIInput();
       UIComponent child111 = new UIInput();
       UIComponent child121 = new UIInput();
       comp1.getChildren().add(child11); 
       comp1.getChildren().add(child12); 
       child11.getChildren().add(child111);
       child12.getChildren().add(child121);
       Object state = comp1.processSaveState(FacesContext.getCurrentInstance());
       comp1.processRestoreState(FacesContext.getCurrentInstance(), state);
    %>
    PASSED
    </body>
</html>
