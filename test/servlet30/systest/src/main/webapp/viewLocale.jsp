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
    <title>Test view tag with Locale</title>
  </head>

  <body>
    <h1>Test view tag with Locale</h1>

    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

    <f:view locale="fr">  
      <h:form id="form">

	<hr>
	<p>Press a button, see some text.</p>
        <h:inputText id="field" required="true" /> 

        <h:commandButton id="button" value="submit" />

        <h:message for="field" />
      </h:form>
    </f:view>



    <hr>
    <address><a href="mailto:Ed Burns <ed.burns@sun.com>"></a></address>
  </body>
</html>
