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

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head> <title>Hello</title> </head>
    <%@ page contentType="application/xhtml+xml" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <body bgcolor="white">
        <f:view>
            <h:form id="form">
                <h:panelGrid columns="2">
                    Input String <h:inputText id="inputText" value="#{bean.stringProperty}"/>
                    Output String <h:outputText id="outputText" value="#{bean.stringProperty}"/>
                    <h:commandButton id="submit" value="reload" />
                </h:panelGrid>
            </h:form>
        </f:view>
        <p>
            <a href="http://validator.w3.org/check?uri=referer"><img
                    src="http://www.w3.org/Icons/valid-xhtml10"
                    alt="Valid XHTML 1.0!" height="31" width="88" /></a>
        </p>
    </body>
</html>  
