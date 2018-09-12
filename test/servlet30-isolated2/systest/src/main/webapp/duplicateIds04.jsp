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

<%--
  - @@copyright@@
  --%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<f:view>
  <html>
    <head>
      <title>Search Criteria</title>
    </head>
    <body>
      <h2>Search for items that:</h2>
      <h:form>
        <h:panelGrid columns="3" border="1" cellpadding="5" cellspacing="0"
                     binding="#{duplicateIds04.panelGrid}" />
        <br>
        <h:commandButton value="redisplay" />
      </h:form>
    </body>
  </html>
</f:view>
