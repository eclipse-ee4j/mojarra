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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:view>
  <html>
    <head>
      <title>Nested Tables</title>
    </head>
    <body>
    
    <p>Test from issue <a href="https://javaserverfaces.dev.java.net/issues/show_bug.cgi?id=62">62</a>.</p>
      <h:form id="form">
        <h:dataTable id="outer" value="#{outer62.model}" var="yyy">
          <h:column>
            outer
            <h:dataTable id="inner" value="#{yyy.model}" var="www">
              <h:column>
                inner
                <h:commandLink action="#{outer62.action}">link</h:commandLink>
             </h:column>
           </h:dataTable>
          </h:column>
         </h:dataTable>
         
         <p>Current Status = <h:outputText value="#{outer62.curStatus}" /></p>
      </h:form>
    </body>
  </html>
</f:view>

