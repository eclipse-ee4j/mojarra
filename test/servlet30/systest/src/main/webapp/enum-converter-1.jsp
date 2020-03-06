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
%><%@ page import="jakarta.faces.component.UIInput" 
%><%@ page import="jakarta.faces.context.FacesContext" 
%><%@ page import="jakarta.faces.convert.ConverterException"
%><%@ page import="jakarta.faces.convert.EnumConverter"
%><%@ page import="com.sun.faces.systest.model.EnumBean"
%><%

  // Test - no targetClass Exception
  EnumConverter enumConverter = new EnumConverter();
  UIInput input = new UIInput();
  input.setId("myInput");
  String msg = null;
  try {
      Object obj = enumConverter.getAsObject(FacesContext.getCurrentInstance(), input, "foo");
  } catch (ConverterException ce) {
      msg = ce.getMessage();
  }
  if (msg.equals("myInput: 'foo' must be convertible to an enum from the enum, but no enum class provided.")) {
      out.println("/enum-converter-1.jsp PASSED");
  } else {
      out.println("/enum-converter-1.jsp FAILED");
  }
      
  try {
      String str = enumConverter.getAsString(FacesContext.getCurrentInstance(), input, EnumBean.Simple.Value1);
  } catch (ConverterException ce) {
      msg = ce.getMessage();
  }
  if (msg.equals("myInput: 'bar' must be convertible to an enum from the enum, but no enum class provided.")) {
      out.println("/enum-converter-1.jsp PASSED");
  } else {
      out.println("/enum-converter-1.jsp FAILED");
  }

  // Test Valid Enum member
  try {
      enumConverter = new EnumConverter(EnumBean.Simple.class);
      String str = enumConverter.getAsString(FacesContext.getCurrentInstance(), input, EnumBean.Simple.Value2);  
      out.println("/enum-converter-1.jsp PASSED");
  } catch (ConverterException ce) {
      out.println("/enum-converter-1.jsp FAILED");
  }

  // Test Invalid Enum member
  try {
      enumConverter = new EnumConverter();
      String str = enumConverter.getAsString(FacesContext.getCurrentInstance(), input, EnumBean.Simple2.Value);  
      out.println("/enum-converter-1.jsp FAILED");
  } catch (ConverterException ce) {
      out.println("/enum-converter-1.jsp PASSED");
  }
%>
