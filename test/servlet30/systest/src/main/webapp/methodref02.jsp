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

<%@ page contentType="text/html"
%><%@ page import="jakarta.faces.application.Application"
%><%@ page import="jakarta.faces.context.FacesContext"
%><%@ page import="jakarta.faces.el.MethodBinding"
%><%@ page import="com.sun.faces.systest.model.TestBean"
%><%@ page import="com.sun.faces.systest.model.TestBeanSubclass"
%><%

  // Instantiate our test bean in request scope
  TestBeanSubclass bean = new TestBeanSubclass();
  FacesContext context = FacesContext.getCurrentInstance();
  context.getExternalContext().getRequestMap().put
   ("testMB", bean);
  MethodBinding mb;
  Class signature[] = new Class[] { String.class };
  Object params[] = new Object[1];
  Object result;

  // Access public methods defined on the bean class itself
  try {
      mb = context.getApplication().createMethodBinding
        ("#{testMB.setExtraProperty}", signature);
      params[0] = "New Extra Property Value";
      mb.invoke(context, params);
  } catch (Exception e) {
    out.println("/methodref02.jsp FAILED - setExtraProperty() exception: " + e);
    e.printStackTrace(System.out);
    return;
  }
  try {
      mb = context.getApplication().createMethodBinding
        ("#{testMB.getExtraProperty}", null);
      result = mb.invoke(context, null);
      if (!params[0].equals(result)) {
          out.println("/methodref02.jsp FAILED - getExtraProperty() returned: " + result);
          return;
      }
  } catch (Exception e) {
    out.println("/methodref02.jsp FAILED - getExtraProperty() exception: " + e);
    e.printStackTrace(System.out);
    return;
  }

  // Access public methods defined on the superclass
  try {
      mb = context.getApplication().createMethodBinding
        ("#{testMB.setStringProperty}", signature);
      params[0] = "New String Property Value";
      mb.invoke(context, params);
  } catch (Exception e) {
    out.println("/methodref02.jsp FAILED - setStringProperty() exception: " + e);
    e.printStackTrace(System.out);
    return;
  }
  try {
      mb = context.getApplication().createMethodBinding
        ("#{testMB.getStringProperty}", null);
      result = mb.invoke(context, null);
      if (!params[0].equals(result)) {
          out.println("/methodref02.jsp FAILED - getStringProperty() returned: " + result);
          return;
      }
  } catch (Exception e) {
    out.println("/methodref02.jsp FAILED - getStringProperty() exception: " + e);
    e.printStackTrace(System.out);
    return;
  }

  // Report success
  out.println("/methodref02.jsp PASSED");

%>
