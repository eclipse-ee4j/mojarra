<!--
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

-->

<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:h="http://java.sun.com/jsf/html"
      xml:lang="en" lang="en">
<jsp:output doctype-root-element="html"
            doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
            doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
<jsp:directive.page contentType="application/xhtml+xml; charset=UTF-8"/>
<head>
    <title>Guess The Number</title>
</head>
<body bgcolor="white">
<f:view>
    <h:form id="responseForm">
        <h:graphicImage id="waveImg" url="/wave.med.gif"/>
        <h2>
            <h:outputText id="result" lang="en"
                          value="#{UserNumberBean.response}"/>
        </h2>
        <h:commandButton id="back" value="Back" action="success"/>
        <p/>

    </h:form>
</f:view>

<p>
    <a href="http://validator.w3.org/check?uri=referer"><img
          src="http://www.w3.org/Icons/valid-xhtml10"
          alt="Valid XHTML 1.0!" height="31" width="88"/></a>
</p>

</body>
</html>
