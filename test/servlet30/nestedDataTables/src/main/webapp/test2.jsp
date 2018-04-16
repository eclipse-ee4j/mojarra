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
    <title>Nested Tables 2</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
  </head>

  <body>
    <h1>Nested Tables 2</h1>

<f:view>

  <h:form id="form">

    <h:dataTable id="outerData" value="#{outer}" var="outerVar">

      <h:column id="outerColumn0">

       <h:commandButton id="outerButton" action="#{bean.outerAction}" 
                        value="outerButton" />

      </h:column>

      <h:column id="outerColumn1">

        <h:dataTable id="innerData" value="#{inner.listDataModel}" 
                     var="innerVar">


          <h:column id="innerColumn">
            <h:commandButton id="innerButton" action="#{bean.innerAction}" 
                       value="innerButton" />
          </h:column>

        </h:dataTable>

      </h:column>

    </h:dataTable>

    <h:commandButton style="color: red" value="reload" />

    <p />

    <p>Outer Action called 
       <h:outputText value="#{bean.outerActionCallCount}" /> times</p>

    <p>Inner Action called 
       <h:outputText value="#{bean.innerActionCallCount}" /> times</p>

    <h:messages />


  </h:form>

</f:view>

  </body>
</html>
