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
    <title>TestViewTag</title>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

  </head>

  <body>
    <h1>TestViewTag</h1>

<f:loadBundle basename="com.sun.faces.TestMessages" var="testMessages" />
<f:view locale="#{testMessages.localeFromBundle}">
<h:form styleClass="formClass" accept="html">

<table>

<tr>
<td>Name:</td>
<td><h:inputText value="Gilligan"/></td>
<td><h:commandButton value="submit"/></td>
</tr>
</table>

</h:form>
</f:view>


    <hr>
    <address><a href="mailto:Ed Burns <ed.burns@sun.com>"></a></address>
<!-- Created: Wed Oct 15 17:31:05 Eastern Daylight Time 2003 -->
<!-- hhmts start -->
Last modified: Tue Oct  5 01:46:04 EDT 2010
<!-- hhmts end -->
  </body>
</html>
