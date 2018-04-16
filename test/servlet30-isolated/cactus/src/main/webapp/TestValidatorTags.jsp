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
    <title>Test Validator Tags</title>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  </head>

  <body>
    <h1>Test Validator Tags</h1>

<f:view>
<h:form id="validatorForm">
<table>

  <tr>

    <td>

                   <h:inputText id="outOfBounds1" value="3.1415">
                       <f:convertNumber pattern="####"/>
                       <f:validateDoubleRange minimum="10.0" 
                                              maximum="10.5"/>
                   </h:inputText>

    </td>


  </tr>

  <tr>

    <td>

                   <h:inputText id="inBounds1" value="10.25">
                     <f:convertNumber pattern="####"/>
                     <f:validateDoubleRange minimum="10.0" 
                                            maximum="10.5"/>
                   </h:inputText>

    </td>


  </tr>

  <tr>

    <td>

                   <h:inputText id="outOfBounds2" value="fox">
                     <f:validateLength minimum="10" maximum="11"/>
                   </h:inputText>

    </td>


  </tr>

  <tr>

    <td>

                   <h:inputText id="inBounds2" value="alligator22">
                     <f:validateLength minimum="10"  maximum="12"/>
                   </h:inputText>

    </td>


  </tr>

  <tr>

    <td>

                   <h:inputText id="outOfBounds3" value="30000">
                     <f:convertNumber  />
                     <f:validateLongRange minimum="100000" maximum="110000"/>
                   </h:inputText>

    </td>


  </tr>

  <tr>

    <td>

                   <h:inputText id="inBounds3" value="1100">
                     <f:convertNumber  />
                     <f:validateLongRange minimum="1000"  maximum="1200"/>
                   </h:inputText>

    </td>


  </tr>

  <tr>

    <td>

                   <h:inputText id="required1" value="required" 
                                 required="true"/>

    </td>


  </tr>

  <tr>

    <td>

                   <h:inputText id="required2" value="required" 
                                 required="true"/>
    </td>


  </tr>

</table>
</h:form>
</f:view>

  </body>
</html>
