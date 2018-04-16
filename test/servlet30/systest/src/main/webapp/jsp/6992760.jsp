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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="javax.faces.context.*"%>
<%@ page import="javax.faces.el.ValueBinding"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<f:view>
  <html>
    <head>
      <meta http-equiv="Content-Type"
            content="text/html; charset=windows-1252"/>
      <title>untitled1</title>
    </head>
    <body><h:form>
    
    <%
      FacesContext fc = FacesContext.getCurrentInstance();
      ValueBinding vb = fc.getApplication().createValueBinding("#{1} #{2}");
      Class type = vb.getType(fc);
      fc.getExternalContext().getRequestMap().put("message", type);
      System.out.println("type:"+type);
    %>
    <p><a href="http://monaco.sfbay.sun.com/detail.jsf?cr=6992760">http://monaco.sfbay.sun.com/detail.jsf?cr=6992760</a></p>
    <p>Message: <h:outputText value="#{message}" /></p>
    </h:form></body>
  </html>
</f:view>
