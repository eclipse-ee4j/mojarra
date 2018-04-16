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

<HTML>
    <HEAD> <TITLE> JSF Basic Components Test Page </TITLE> </HEAD>

    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

    <BODY>
        <H3> JSF Basic Components Test Page </H3>

<f:view>
<h:form id="basicForm">

  <TABLE BORDER="1">

      <TR>
      <td>
                    <h:inputText id="userName" 
                                     value="JavaServerFaces" >
		     <f:validateLength minimum="6" maximum="10"/>
		     <f:validateRequired/>
                   </h:inputText>

              </td>


	<TD>

	      <h:commandLink id="link" href="hello.html"
                           styleClass="hyperlinkClass"
				       value="link text"/>

	</TD>

      </TR>

      <TR>

	<TD>

            <h:selectManyCheckbox id="validUser" 
                   styleClass="selectbooleanClass"/>
	</TD>

      </TR>

      <TR>

	<TD>

	     <h:selectOneListbox id="appleQuantity" 
                     title="Select Quantity"
                     accesskey="N" tabindex="20" >

                <f:selectItem  disabled="true" itemValue="0" itemLabel="0"/>
                <f:selectItem  itemValue="4" itemLabel="4" title="Four" selected="true"/>
                <f:selectItem  itemValue="9" itemLabel="9" title="nine" />

              </h:selectOneListbox>

	</TD>

      </TR>

					<TD><h:selectManyMenu id="ManyApples">
						<f:selectItem itemValue="4" itemLabel="four" selected="true" />
						<f:selectItem itemValue="6" itemLabel="six" />
						<f:selectItem itemValue="7" itemLabel="seven" selected="true" />
					</h:selectManyMenu></TD>

</tr>

  </TABLE>

</h:form>
</f:view>

    </BODY>
</HTML>
