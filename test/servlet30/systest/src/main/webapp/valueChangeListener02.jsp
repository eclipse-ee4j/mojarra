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
    <title>ValueChangeListeners and Validators</title>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
</head>

<body>
<h1>ValueChangeListeners</h1>

<f:view>
    <h:form>
        <h:selectBooleanCheckbox value="true">
            <f:valueChangeListener type="com.sun.faces.systest.model.TestValueChangeListener"/>
        </h:selectBooleanCheckbox>
        <h:commandButton value="Click"/>
    </h:form>
    <h:messages/>
</f:view>

</body>
</html>
