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



<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<f:view>
<html>
<head>
  <title>
    CharacterCombat Main Page
  </title>
  <link rel="stylesheet" type="text/css"
    href='<%= request.getContextPath() + "/stylesheet.css" %>'>
</head>

<body>

  <h:graphicImage url="/images/header.jpg" />

  <h2>Welcome to the Character Combat</h2>
  <p>This sample application illustrates how you can easily display data
     from a backing bean, process user input, handle navigation, and
     display the results all using the JavaServer Faces Framework</p>
<p>This initial page displays a list of available characters in a table
     format. You can choose to add your own character to the list using
     the input text field or simply go on to the next page</p>

  <h:dataTable columnClasses="list-column-center,
                              list-column-center,
                              list-column-center,
                              list-column-center"
               headerClass="list-header"
               styleClass="list-background"
               value="#{modelBean.dataList}"
               var="character" >

    <f:facet name="header">
      <h:outputText value="List of Available Characters"/>
    </f:facet>

    <h:column>
      <f:facet name="header">
        <h:outputText value="Name"/>
      </f:facet>

      <h:outputText value="#{character.name}"/>

    </h:column>

    <h:column>
      <f:facet name="header">
        <h:outputText value="Species"/>
      </f:facet>

      <h:outputText value="#{character.species.type}"/>

    </h:column>

    <h:column>
      <f:facet name="header">
        <h:outputText value="Language"/>
      </f:facet>

      <h:outputText value="#{character.species.language}"/>

    </h:column>

    <h:column>
      <f:facet name="header">
        <h:outputText value="Immortal"/>
      </f:facet>

      <h:outputText value="#{character.species.immortal}"/>

    </h:column>

  </h:dataTable>

<br>

  <h:form prependId="false">
    <h:panelGrid columnClasses="list-column-center,
                                list-column-center"
                 headerClass="list-header"
                 styleClass="inputList-background"
                 columns="2">
      <f:facet name="header">
        <h:outputText value="Customize Character:"/>
      </f:facet>
      <h:inputText value="#{modelBean.customName}" />
      <h:selectOneListbox value="#{modelBean.customSpecies}"
        required="true" size="1" >
        <f:selectItems value="#{modelBean.speciesOptions}"/>
      </h:selectOneListbox>
    </h:panelGrid>
<br>
    <h:panelGrid columns="1">
      <h:commandButton actionListener="#{modelBean.addCustomName}" 
                       value="Add Name"/>
    </h:panelGrid>

    <jsp:include page="wizard-buttons.jsp"/>


  </h:form>

</body>

</html>
</f:view>
