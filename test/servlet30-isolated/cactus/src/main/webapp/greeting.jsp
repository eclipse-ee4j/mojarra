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



<HTML>
    <HEAD> <title>Hello</title> </HEAD>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <body bgcolor="white">
    <h2>Hi. My name is Duke.  I'm thinking of a number from 0 to 10.
    Can you guess it?</h2>
    <f:view>
    <h:form id="helloForm" >
  	<h:inputText id="userNo"  value="NUMBER" /> <BR>

	 <h:commandButton id="submit" value="Submit" />
    </h:form>
    </f:view>
</HTML>  
