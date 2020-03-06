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
    <title>JSP view</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
  </head>

  <body>
<f:view>

<p>HTML Template Text</p>

<p><h:outputText value="#{processAsJspxBean.prop}" /></p>

<h:form prependId="false">

<h:commandButton value="reload" />

</h:form>

                  <code>
                    <br/>
                    &amp;lt;context-param&amp;gt;
                    <br/>
                     
&amp;nbsp;&amp;nbsp;&amp;lt;param-name&amp;gt;jakarta.faces.PARTIAL_STATE_SAVING 

&amp;lt;/param-name&amp;gt;
                    <br/>
                     
&amp;nbsp;&amp;nbsp;&amp;lt;param-value&amp;gt;true&amp;lt;/param-value&amp;gt 

;
                    <br/>
                    &amp;lt;/context-param&amp;gt;
                  </code>      


<jsp:text>
  some text
</jsp:text>

<jsp:plugin 
   type="applet" 
   code="Blink.class" 
   codebase="."
   name="Arthur van Hoff"
   align="baseline"
   width="300"
   height="200"
   hspace="20"
   vspace="20"
   jreversion="1.6">
  <jsp:params>
    <jsp:param name="lbl" value="This is the next best thing to sliced bread! Toast, toast, toast, butter, jam, toast, marmite, toast." /> 
  </jsp:params>
  <jsp:fallback>
No JDK 1.3 support for APPLET!!
  </jsp:fallback>
</jsp:plugin>

</f:view>

    <hr>
  </body>
</html>
