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

<HTML>

    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

       <jsp:useBean id="TestBean" class="com.sun.faces.systest.model.TestBean" scope="session" />
       <f:view renderKitId="HTML_BASIC" >  
          <h:form id="form">
              <table>
              <tr>
                 <td><h:outputText value="RenderKit:" /></td> 
                 <td><h:outputText value="#{TestBean.renderKitInfo}" /></td>
              </tr>
              <tr>
                 <td><h:outputText value="ResponseWriter:" /></td> 
                 <td><h:outputText value="#{TestBean.responseWriterInfo}" /></td> 
              </tr>
              </table>
              <h:commandButton id="submit" action="success" value="submit"/>
          </h:form>
       </f:view>

</HTML>
