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
                                                                                
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
                                                                                
                                                                                
<html>
  <body>
     <f:view>
     <h:form id="form01">
       <h:commandLink id ="Link1" action="redirect">
          <h:outputText value="Link1"/>
          <f:param id="hlParam1" name="param1" value="value1"/>
          <f:param id="hlParam2" name="param2" value="value2"/>
       </h:commandLink>
       <h:commandLink id ="Link2" action="redirect">
          <h:outputText value="Link2"/>
          <f:param id="hlParam3" name="param1" value="value1"/>
          <f:param id="hlParam4" name="param2" value="value2"/>
       </h:commandLink>
    </h:form> 
    <h:form id="form02">
       <h:commandLink id ="Link3" action="redirect">
          <h:outputText value="Link3"/>
          <f:param id="hlParam1" name="param3" value="value3"/>
          <f:param id="hlParam2" name="param4" value="value4"/>
       </h:commandLink>
       <h:commandLink id ="Link4" action="forward">
          <h:outputText value="Link4"/>
       </h:commandLink>
       <h:commandLink value="Link5">
           <f:param id="l5param1" name="#{null}" value="should_not_be_present"/>
           <f:param id="l5param2" name="param5" value="#{null}"/>
       </h:commandLink>
   </h:form>
   </f:view>
  </body>
</html>
