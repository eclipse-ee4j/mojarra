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
    <title>Test Tag ivars are cleared properly</title>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

  </head>

  <body>
    <h1>Test Tag ivars are cleared properly</h1>

<p><a
href="https://javaserverfaces.dev.java.net/issues/show_bug.cgi?id=36">https://javaserverfaces.dev.java.net/issues/show_bug.cgi?id=36</a></p>


<f:view>

<h:form>

<p>  <h:outputText value="component 1" /> </p>

<p>  <h:outputText value="component 2" /> </p>

<p>  <h:outputText value="component 3" /> </p>

</h:form>

</f:view>

    <hr>
    <address><a href="mailto:b_edward@bellsouth.net">Ed Burns</a></address>
<!-- Created: Tue Aug 31 13:26:22 EDT 2004 -->
<!-- hhmts start -->
<!-- hhmts end -->
  </body>
</html>
