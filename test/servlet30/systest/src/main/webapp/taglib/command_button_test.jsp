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
<head>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <title>command_button_test.jsp</title>
</head>
<body>
    
    <f:loadBundle basename="com.sun.faces.systest.resources.Resources" 
        var="messageBundle"/>
    <f:view locale="en_US">
      <h:form id="form01">
        <h:command_button id="button01" type="submit" value="My Label"/>
        <h:command_button id="button02" type="reset" value="#{test1.stringProperty}"/>
        <h:command_button id="button03" type="submit" value="#{messageBundle.button_key}"/>
        <h:command_button id="button04" type="reset" image="duke.gif" value="FAIL"/>
        <h:command_button id="button05" type="submit" image="#{messageBundle.image_key}"/>
      </h:form>
    </f:view>
</body>
</html>

