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
    <title>Validators</title>
    <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
  </head>

  <body>
    <h1>Validators</h1>

<f:view>

  <h:form>

    literal required: <h:inputText id="textField" required="true" 
                 requiredMessage="Literal Message" />

    <p/>

    expression required <h:inputText id="textField2" required="true" 
                 requiredMessage="#{test2.stringProperty}" />

    <p/>
    
    literal converter <h:inputText id="textField3" value="#{test2.intProperty}" 
                         converterMessage="Converter Literal" />

    <p/>

    expression converter <h:inputText id="textField4" value="#{test2.intProperty}" 
                         converterMessage="#{test2.converterMessage}" />

    <p/>
    
    literal validator <h:inputText id="textField5" value="#{test2.intProperty}" 
                         validatorMessage="Validator Literal">
                         <f:validateLongRange minimum="1" maximum="10" />
                      </h:inputText>

    <p/>

    expression validator <h:inputText id="textField6" value="#{test2.intProperty}" 
                         validatorMessage="#{test2.validatorMessage}">
                         <f:validateLongRange minimum="1" maximum="10" />
                      </h:inputText>

    <p/>
    

    <h:messages />

    <h:commandButton value="submit" />

  </h:form>

</f:view>

    <hr>
  </body>
</html>
