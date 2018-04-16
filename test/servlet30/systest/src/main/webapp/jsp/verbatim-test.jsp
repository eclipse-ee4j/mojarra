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
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

    <f:view>
      <h:form id="form1">
        <table>
        <tr>
         <td>
         <h:panelGrid id="panelGrid" styleClass="scrollPane" columns="1">
           <h:panelGroup id="panelGroup">
             <f:verbatim><DIV STYLE="overflow: auto; height: 100px;"></f:verbatim>
             <h:outputText id="outputtext" value="An output text"/>
             <f:verbatim></DIV></f:verbatim>
           </h:panelGroup>
         </h:panelGrid>
         </td>
        </tr>
        <tr>
         <td>
         <h:commandButton id="submit" value="submit" action="success"/>
         </td>
        </tr>
        </table>
      </h:form>
    </f:view>
</html>

