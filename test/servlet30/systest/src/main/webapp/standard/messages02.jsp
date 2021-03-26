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

<%@ page contentType="text/html" %>
<%@ page import="jakarta.faces.application.FacesMessage"%>
<%@ page import="jakarta.faces.context.FacesContext"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
                                                                                    
<%
  // Acquire the FacesContext instance for this request
  FacesContext facesContext = FacesContext.getCurrentInstance();
  if (facesContext == null) {
    out.println("/messages02.jsp FAILED - No FacesContext returned");
    return;
  }
  FacesMessage imsg = new FacesMessage(FacesMessage.SEVERITY_INFO,
      "Information Summary", "Informational Detail");
  FacesMessage wmsg = new FacesMessage(FacesMessage.SEVERITY_WARN,
      "Warning Summary", "Warning Detail");
  FacesMessage emsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
      "Error Summary", "Error Detail");
  FacesMessage fmsg = new FacesMessage(FacesMessage.SEVERITY_FATAL,
      "Fatal Summary", "Fatal Detail");
  facesContext.addMessage("1", imsg);
  facesContext.addMessage("1", wmsg);
  facesContext.addMessage("1", emsg);
  facesContext.addMessage("1", fmsg);
%>
                                                                                    
<html>
<STYLE TYPE="text/css" MEDIA=screen>
<!--
.errors {
  background-color: #7171A5;
  border: 5px outset #71A5A5;
  border-collapse: collapse;
  font-family: sans-serif;
  font-size: 14pt;
  padding: 10px;
  left: 48px;
  top: 300px;
  position: absolute;
}
-->
</STYLE>

<f:view>
      <h:panelGrid columns="1"> 
         <h:messages layout="list" 
            style="left: 48px; top: 100px; position: absolute"
            dir="LTR"
            infoStyle="color: yellow"
            errorStyle="color: red"
            fatalStyle="color: blue"
            showSummary="true" showDetail="true" tooltip="true"/>

         <h:messages layout="table" 
            style="left: 48px; top: 200px; position: absolute"
            lang="en"
            infoStyle="color: yellow"
            errorStyle="color: red"
            fatalStyle="color: blue"
            showSummary="true" showDetail="true" tooltip="true"/>

         <h:messages  
            styleClass="errors"
            showSummary="true" showDetail="true" />

         <h:messages layout="table" 
            style="left: 48px; top: 500px; position: absolute"
            infoStyle="color: yellow"
            errorStyle="color: red"
            fatalStyle="color: blue"
            showDetail="true" />
       </h:panelGrid>
</f:view>
</html>
