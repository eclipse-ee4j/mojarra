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
%><%@ page import="javax.faces.FactoryFinder"
%><%@ page import="javax.faces.application.Application"
%><%@ page import="javax.faces.application.ApplicationFactory"
%><%@ page import="javax.faces.context.FacesContext"
%><%@ page import="javax.el.ValueExpression"
%><%@ page import="com.sun.faces.systest.model.TestBean"
%><%

  // Instantiate a managed bean and validate property values #1

  // Acquire the FacesContext instance for this request
  FacesContext facesContext = FacesContext.getCurrentInstance();
  if (facesContext == null) {
    out.println("/component01.jsp FAILED - No FacesContext returned");
    return;
  }
  // Acquire our Application instance
  ApplicationFactory afactory = (ApplicationFactory)
   FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
  Application appl = afactory.getApplication();

  // Acquire a ValueExpression for the bean to be created
  ValueExpression valueExpression = appl.getExpressionFactory().createValueExpression(facesContext.getELContext(),"#{test1}", 
      Object.class);
  if (valueExpression == null) {
    out.println("/managed01.jsp FAILED - No ValueExpression returned");
    return;
  }

  // Evaluate the value binding and check for bean creation
  Object result = valueExpression.getValue(facesContext.getELContext());
  if (result == null) {
    out.println("/managed01.jsp FAILED - getValue() returned null");
    return;
  }
  if (!(result instanceof TestBean)) {
    out.println("/managed01.jsp FAILED - result of type " + result.getClass());
    return;
  }
  Object scopedFromExternalContext = ((javax.servlet.http.HttpServletRequest)facesContext.getExternalContext().getRequest()).getAttribute("test1");
  if (scopedFromExternalContext == null) {
    out.println("/managed01.jsp FAILED - not created in request scope, from ExternalContext request ");
    return;
  }
  Object scopedFromMap = facesContext.getExternalContext().getRequestMap().get("test1");
  if (scopedFromMap == null) {
    out.println("/managed01.jsp FAILED - not created in request scope, from ExternalContext Map");
    return;
  }


  Object scoped = request.getAttribute("test1");
  if (scoped == null) {
    out.println("/managed01.jsp FAILED - not created in request scope");
    return;
  }
  if (!(result == scoped)) {
    out.println("/managed01.jsp FAILED - created bean not same as attribute");
    return;
  }

  // Verify the property values of the created bean
  TestBean bean = (TestBean) result;
  StringBuffer sb = new StringBuffer();
  if (!bean.getBooleanProperty()) {
    sb.append("booleanProperty(" + bean.getBooleanProperty() + ")|");
  }
  if ((byte) 12 != bean.getByteProperty()) {
    sb.append("byteProperty(" + bean.getByteProperty() + ")|");
  }
  if (123.45 != bean.getDoubleProperty()) {
    sb.append("doubleProperty(" + bean.getDoubleProperty() + ")|");
  }
  if ((float) 12.34 != bean.getFloatProperty()) {
    sb.append("floatProperty(" + bean.getFloatProperty() + ")|");
  }
  if (123 != bean.getIntProperty()) {
    sb.append("intProperty(" + bean.getIntProperty() + ")|");
  }
  if (12345 != bean.getLongProperty()) {
    sb.append("longProperty(" + bean.getLongProperty() + ")|");
  }
  if ((short) 1234 != bean.getShortProperty()) {
    sb.append("shortProperty(" + bean.getShortProperty() + ")|");
  }
  if (!"This is a String property".equals(bean.getStringProperty())) {
    sb.append("stringProperty(" + bean.getStringProperty() + ")|");
  }

  // Report any property errors
  String errors = sb.toString();
  if (errors.length() < 1) {
    out.println("/managed01.jsp PASSED");
  } else {
    out.println("/managed01.jsp FAILED - property value errors:  " + errors);
  }
%>
