<%--

    Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.

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

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Mojarra Agnostic Composite Component Tests</title>
    </head>
    <body>
        <h1>Mojarra Agnostic Composite Component Tests</h1>
        <h3>Simple</h3>
        <ul>
            <li><a href="faces/basic/isInsideCompositeComponent.xhtml">Test to see if we are inside a composite component</a></li>
            <li><a href="faces/basic/isInsideCompositeComponent2.xhtml">Test to see if we are inside a composite component #2</a></li>
            <li><a href="faces/basic/simple1.xhtml">A simple composite component</a></li>
            <li><a href="faces/basic/simple2.xhtml">A h:outputText inside a composite component</a></li>
            <li><a href="faces/basic/simple3.xhtml">A h:outputText inside a composite component (using EL)</a></li>
        </ul>
        <h3>Nested</h3>
        <ul>
            <li><a href="faces/nested/nested1.xhtml">A nested composite component test</a></li>
            <li><a href="faces/nested/nested2.xhtml">A h:outputText inside a nested composite component</a></li>
            <li><a href="faces/nested/nested3.xhtml">A h:outputText inside a nested composite component (using EL)</a></li>
        </ul>
        <h3>cc:actionSource</h3>
        <ul>
            <li><a href="faces/actionSource/actionSource1.xhtml">A cc:actionSource test</a></li>
        </ul>
        <h3>cc:attribute and # {cc.attrs}</h3>
        <ul>
            <li><a href="faces/attribute/attribute1.xhtml">A composite component using # {cc.attrs.text}</a></li>
            <li><a href="faces/attribute/attribute2.xhtml">A composite component using cc:attribute with a default and overriding the default</a></li>
        </ul>
        <h3>cc:attribute and actions</h3>
        <ul>
            <li><a href="faces/action/action1.xhtml">A custom action</a></li>
        </ul>
        <h3>cc:insertChildren</h3>
        <ul>
            <li><a href="faces/insertChildren/insertChildren1.xhtml">A cc:insertChildren inside a composite component</a></li>
            <li><a href="faces/insertChildren/insertChildren2.xhtml">A cc:insertChildren inside a nested composite component</a></li>
            <li><a href="faces/insertChildren/insertChildren3.xhtml">A cc:insertChildren inside a composite component with an inactive facet</a></li>
            <li><a href="faces/insertChildren/insertChildren4.xhtml">A cc:insertChildren inside of panelGroup inside a composite component</a></li>
        </ul>
        <h3>cc:insertFacet</h3>
        <ul>
            <li><a href="faces/insertFacet/insertFacet1.xhtml">A composite component with cc:insertFacet in it</a></li>
        </ul>
        <h3>cc:renderFacet</h3>
        <ul>
            <li><a href="faces/renderFacet/renderFacet1.xhtml">A composite component using cc:renderFacet</a></li>
            <li><a href="faces/renderFacet/renderFacet2.xhtml">A composite component using cc:renderFacet #2</a></li>
        </ul>
        <h3>h:input</h3>
        <ul>
            <li><a href="faces/input/input1.xhtml">A composite component with a UIInput in it</a></li>
        </ul>
        <h3>ui:define</h3>
        <ul>
            <li><a href="faces/define/define1.xhtml">A composite component with template functionality in it</a></li>
        </ul>
    </ul>
</body>
</html>
