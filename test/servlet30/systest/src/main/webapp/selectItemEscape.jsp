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

\<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">



<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
  <head> <title>Test SelectItem with escape true and false</title> </head>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <body bgcolor="white">
    <f:view>

      <h:form prependId="false">

<h:panelGrid columns="2">

  SelectOneMenu with no escape value

<h:selectOneMenu id="menu1">
						<f:selectItem itemValue="10" itemLabel="menu1_Guy <Lafleur>" />
						<f:selectItem itemValue="99" itemLabel="menu1_Wayne <Gretzky>" />
						<f:selectItem itemValue="4" itemLabel="menu1_Bobby +Orr+"  />
						<f:selectItem itemValue="2" itemLabel="menu1_Brad &{Park}" />
						<f:selectItem itemValue="9" itemLabel="menu1_Gordie &Howe&" />
					</h:selectOneMenu>
SelectOneMenu with true escape value

<h:selectOneMenu id="menu2">
						<f:selectItem escape="true" itemValue="10" itemLabel="menu2_Guy <Lafleur>" />
						<f:selectItem escape="true" itemValue="99" itemLabel="menu2_Wayne <Gretzky>" />
						<f:selectItem escape="true" itemValue="4" itemLabel="menu2_Bobby +Orr+"  />
						<f:selectItem escape="true" itemValue="2" itemLabel="menu2_Brad &{Park}" />
						<f:selectItem escape="true" itemValue="9" itemLabel="menu2_Gordie &Howe&" />
					</h:selectOneMenu>

SelectOneMenu with false escape value

<h:selectOneMenu id="menu3">
						<f:selectItem escape="false" itemValue="10" itemLabel="menu3_Guy <Lafleur>" />
						<f:selectItem escape="false" itemValue="99" itemLabel="menu3_Wayne <Gretzky>" />
						<f:selectItem escape="false" itemValue="4" itemLabel="menu3_Bobby +Orr+"  />
						<f:selectItem escape="false" itemValue="2" itemLabel="menu3_Brad &{Park}" />
						<f:selectItem escape="false" itemValue="9" itemLabel="menu3_Gordie &Howe&" />
					</h:selectOneMenu>


</h:panelGrid>


      </h:form>

    </f:view>
    </body>
</html>  
