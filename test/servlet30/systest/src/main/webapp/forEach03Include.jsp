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

<%@ page contentType="text/html" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<c:forEach var="item" items="#{input1}">
    <h:outputLabel for="inputId11" value="Label for #{item.key} below"/>
    <h:message for="inputId11" styleClass="message"/>
    <h:inputText id="inputId11" value="#{input1[item.key]}" required="true"/>
</c:forEach>

<h:inputText id="Short11" value="#{forEachBean1.longProperty}"
             required="true"/>
<h:message for="Short11" styleClass="message"/>
<h:outputLabel id="Short11Label" for="Short11"
               value="Label for shortProperty above"/>
