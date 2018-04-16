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
    <title>Test Core Tags VB Enabling</title>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  </head>

  <body>
    <h1>Test Core Tags VB Enabling</h1>

<f:view>
<h:form id="validatorForm">

<table>

  <tr>

    <td>

                   <h:inputText id="doubleRange" value="1000.0">
                        <f:convertNumber type="number" integerOnly="false" 
                           maxFractionDigits="2" maxIntegerDigits="5" locale="en"
                           groupingUsed="true" />
                       <f:validateDoubleRange minimum="#{doubleMin}" 
                                             maximum="#{doubleMax}"/>
                   </h:inputText>

    </td>


  </tr>

  <tr>

    <td>
         <h:inputText id="longRange" value="1000">
                     <f:convertNumber pattern="####" 
                           minFractionDigits="0" minIntegerDigits="2" />
                     <f:validateLongRange minimum="#{longMin}" 
                                           maximum="#{longMax}"/>
         </h:inputText>

    </td>

     <h:outputText id="outputNumber2" value="$123.45">
                   <f:convertNumber type="currency" currencySymbol="$" />
               </h:outputText>

    <h:outputText id="outputDatetime3" value="7/10/96 12:31:31 PM PDT">
       <f:convertDateTime type="both" timeStyle="full" dateStyle="short" 
            locale="en"/>
    </h:outputText>

      

  </tr>

  <tr>

    <td>
            <h:inputText id="intRange" value="NorthAmerica">
                  
                     <f:validateLength minimum="#{intMin}" 
                                       maximum="#{intMax}"/>
             </h:inputText>

    </td>


  </tr>

</table>

</h:form>
</f:view>

  </body>
</html>
