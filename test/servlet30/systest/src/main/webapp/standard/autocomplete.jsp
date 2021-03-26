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
    <title>autocomplete.jsp</title>

 <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
 <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

  </head>
  <body>
      <f:view>
          autocomplete on -&gt; no attribute rendered: <h:inputSecret id="a" autocomplete="on"/>
          autocomplete off -&gt; attribute rendered: <h:inputSecret id="b" autocomplete="off"/>
          no autocomplete defined -&gt; no attribute rendered: <h:inputSecret id="c"/>
          autocomplete on -&gt; no attribute rendered: <h:inputText id="d" autocomplete="on"/>
          autocomplete off -&gt; attribute rendered: <h:inputText id="e" autocomplete="off"/>
          no autocomplete defined -&gt; no attribute rendered: <h:inputText id="f"/>
      </f:view>
  </body>
</html>


