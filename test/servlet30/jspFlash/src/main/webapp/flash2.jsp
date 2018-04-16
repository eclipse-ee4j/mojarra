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
    <title>RoR Flash Test Page 2</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

  </head>

  <body>
    <h1>RoR Flash Test Page 2</h1>

<f:view>

  <p>As mentioned in the previous page, if I wanted to store something
  in the flash during this request and also access it during this
  request, <code>\#{flash.now.bar}</code> is the way to do it.  In
  reality, this just puts the value in request scope, but that's what
  "now" is, anyway.</p>

  <h:form prependId="false" id="form1">

  <h:panelGrid columns="2" border="1">

    Value of the previous request's foo

    <h:outputText id="flash2FooValueId" value="#{flash.foo}" />

    Put <code>barValue</code> in the flash.now under key
    <code>bar</code>.

    <c:set target="${flash.now}" property="bar" value="barValue" />

    <f:verbatim>
      &lt;c:set target="\${flash.now}" property="bar" value="barValue" /&gt;
    </f:verbatim>

    Value of <code>\#{flash.now.bar}</code>, should be <code>barValue</code>.

    <h:outputText id="flash2BarValueId" value="#{flash.now.bar}" />

    <h:commandButton id="reload" value="reload" />

    <h:commandButton id="back" value="back" action="back" />

    &nbsp;

    <h:commandButton id="next" value="next" action="next" />

   </h:panelGrid>

   <p><h:messages id="messages"/></p>

  </h:form>

</f:view>
  </body>
</html>
