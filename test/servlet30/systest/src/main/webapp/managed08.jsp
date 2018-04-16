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
    <title>Managed Bean Lifecycle Annotations</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
  </head>

  <body>
    <h1>Managed Bean Lifecycle Annotations</h1>

<f:view>

  <h:form id="form">
  
  <h:outputText value="#{noneBean.appendRequestMarker}" />

  <p>requestBean PostConstruct: <h:outputText value="#{requestBean.postConstructCalled}" /></p>
  <p>requestBean PreDestroy: <h:outputText value="#{requestBean.preDestroyCalled}" /></p>

  <p>sessionBean PostConstruct: <h:outputText value="#{sessionBean.postConstructCalled}" /></p>
  <p>sessionBean PreDestroy: <h:outputText value="#{sessionBean.preDestroyCalled}" /></p>

  <p>applicationBean PostConstruct: <h:outputText value="#{applicationBean.postConstructCalled}" /></p>
  <p>applicationBean PreDestroy: <h:outputText value="#{applicationBean.preDestroyCalled}" /></p>
  
  <p>previous request status: <pre><h:outputText value="#{applicationScope.previousRequestStatus}" /></pre></p>
  
  <p><h:commandButton id="removeRequestBean" value="remove request bean" action="#{noneBean.removeRequestBean}" /></p>

  <p><h:commandButton id="removeRequestBean2" value="remove request bean2" action="#{noneBean.removeRequestBean2}" /></p>

  <p><h:commandButton id="replaceRequestBean" value="replace request bean" action="#{noneBean.replaceRequestBean}" /></p>

  <p><h:commandButton id="replaceRequestBean2" value="replace request bean2" action="#{noneBean.replaceRequestBean2}" /></p>
       
  <p><h:commandButton id="removeSessionBean" value="remove session bean" action="#{noneBean.removeSessionBean}" /></p>

  <p><h:commandButton id="removeSessionBean2" value="remove session bean2" action="#{noneBean.removeSessionBean2}" /></p>

  <p><h:commandButton id="replaceSessionBean" value="replace session bean" action="#{noneBean.replaceSessionBean}" /></p>

  <p><h:commandButton id="replaceSessionBean2" value="replace session bean2" action="#{noneBean.replaceSessionBean2}" /></p>

  <p><h:commandButton id="removeApplicationBean" value="remove application bean" action="#{noneBean.removeApplicationBean}" /></p>

  <p><h:commandButton id="removeApplicationBean2" value="remove application bean2" action="#{noneBean.removeApplicationBean2}" /></p>

  <p><h:commandButton id="replaceApplicationBean" value="replace application bean" action="#{noneBean.replaceApplicationBean}" /></p>

  <p><h:commandButton id="replaceApplicationBean2" value="replace application bean2" action="#{noneBean.replaceApplicationBean2}" /></p>
       
  <p><h:commandButton id="invalidateSession" value="invalidate session" action="#{noneBean.invalidateSession}" /></p>

  <p><h:commandButton id="clearRequestMap" value="clear request map" action="#{noneBean.clearRequestMap}" /></p>

  <p><h:commandButton id="clearRequestMapTwice" value="clear request map twice" action="#{noneBean.clearRequestMapTwice}" /></p>

  <p><h:commandButton id="clearSessionMap" value="clear session map" action="#{noneBean.clearSessionMap}" /></p>

  <p><h:commandButton id="clearSessionMapTwice" value="clear session map twice" action="#{noneBean.clearSessionMapTwice}" /></p>

  <p><h:commandButton id="clearStatusMessage" value="clear status message" action="#{noneBean.clearStatusMessage}" /></p>
  
  <p><h:commandButton id="reload" value="reload" />
  </h:form>

</f:view>
    <hr>
  </body>
</html>
