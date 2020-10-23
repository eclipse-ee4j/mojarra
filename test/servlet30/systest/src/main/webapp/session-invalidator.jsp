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
    <title>Test with action that invalidates a session.</title>
    <%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
    <%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
  </head>

  <body>
    <h1>Test with action that invalidates a session.</h1>

    <f:view>

     <h:form id="form">

      <table border="1">

       <tr>

	 <td>

	    Next cell's contents come from a bean in session scope.

	 </td>

	 <td>

	    <h:outputText value="#{test3.stringProperty}" style="color: red"/>

	 </td>

	 <td>

	   <h:commandButton action="#{methodRef.invalidateSession}"
                          id="button1"
                          value="Press to invalidate session and redisplay" />

	 </td>

       </tr>

      </table>
   
      </h:form>

    </f:view>


  </body>
</html>
