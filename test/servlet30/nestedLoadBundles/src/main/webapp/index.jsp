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

<%@ page contentType="text/html" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>


<html>
<head>
<title>subview01</title>
</head>
<body>

<p>Correct nesting order of ResourceBundles</p>

<pre><code>

viewRoot

  BundleA

  output01

  BundleB

  output02

  subview id=inner02

    Bundle C

    output03

  output04

  subview id=outer01

    BundleD

    output05

  subview id=outer02

    BundleE

    output06

  button


</code></pre>


<f:loadBundle var="bundle" basename="com.sun.faces.test.servlet30.nestedloadbundles.BundleA" />

<f:view>

<p>Output 01 from bundle: <h:outputText id="outputO1" value="#{bundle.okLabel}" /></p>

<h:outputText value="Begin test <c:import> with subview tag in imported page"/>

<p>
<c:import url="foo01.jsp"/>
</p>

<p><h:outputText value="subview01"/></p>

<p><c:import url="bar01.jsp"/></p>

<p><h:outputText value="End test <c:import> with subview tag in imported page"/></p>

<p>Output 04 from bundle: <h:outputText id="output04" value="#{bundle.okLabel}" /></p>

<p>
<h:outputText value="Begin test <c:import> with subview tag in importing page"/>
</p>

<p><f:subview id="outer01">
<c:import url="foo02.jsp"/>
</f:subview></p>


<p><h:outputText value="subview02"/></p>

<p><f:subview id="outer02">
<c:import url="bar02.jsp"/>
</f:subview>
</p>

<p><h:outputText value="End test <c:import> with subview tag in importing page"/></p>

<h:form prependId="false">

<p>Reload: <h:commandButton id="button" value="#{bundle.okLabel}" /></p>

</h:form>

</f:view>

</body>
</html>
