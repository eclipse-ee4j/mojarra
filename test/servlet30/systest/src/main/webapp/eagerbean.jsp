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

<%@ page contentType="text/html" language="java" %>
<%@ page import="javax.faces.context.FacesContext, java.util.Map" %>
<%
    FacesContext ctx = FacesContext.getCurrentInstance();
    Map<String,Object> appMap = ctx.getExternalContext().getApplicationMap();
    Map<String,Object> sesMap = ctx.getExternalContext().getSessionMap();
    Map<String,Object> reqMap = ctx.getExternalContext().getRequestMap();

    if (appMap.containsKey("eagerApp1")) {
        throw new RuntimeException();
    }
    if (!appMap.containsKey("eagerApp2")) {
        throw new RuntimeException();
    }
    if (appMap.containsKey("eagerApp3")) {
        throw new RuntimeException();
    }
    if (appMap.containsKey("eagerSes1")) {
        throw new RuntimeException();
    }
    if (appMap.containsKey("eagerReq1")) {
        throw new RuntimeException();
    }
%>
