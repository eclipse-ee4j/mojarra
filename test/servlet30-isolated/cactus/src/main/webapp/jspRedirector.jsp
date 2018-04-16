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

--%><%@page import="org.apache.cactus.server.*,org.apache.cactus.internal.server.*" session="true" %><%

    /**                                                
     * Note:
     * It is very important not to put any character between the end
     * of the page tag and the beginning of the java code expression, otherwise,
     * the generated servlet containss a 'out.println("\r\n");' and this breaks
     * our mechanism !
     */

    /**
     * This JSP is used as a proxy to call your server-side unit tests. We use
     * a JSP rather than a servlet because for testing custom JSP tags for
     * example we need access to JSP implicit objects (PageContext and
     * JspWriter).
     */

    JspImplicitObjects objects = new JspImplicitObjects();
    objects.setHttpServletRequest(request);
    objects.setHttpServletResponse(response);
    objects.setServletConfig(config);
    objects.setServletContext(application);
    objects.setJspWriter(out);
    objects.setPageContext(pageContext);

    JspTestRedirector redirector = new JspTestRedirector();
    redirector.doGet(objects);
%>
