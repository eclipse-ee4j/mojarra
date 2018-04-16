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

	<TD>

	      <h:inputText id="userName" value="Default_username" />

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:inputSecret id="password" value="Default_password" />

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:commandButton id="login" value="Login" 
				    action="login"/>

	</TD>

      </TR>


      <TR>

	<TD>

	      <h:commandButton id="imageButton" image="duke.gif"
				    action="login"/>

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:commandLink id="link"
				       value="link text"/>

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:commandLink id="imageLink"

                       value="duke.gif"/>

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:outputText id="userLabel" value="Output Text" />

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:selectManyCheckbox id="validUser" label="Valid User"/>

    </TD>

      </TR>

      <TR>

	<TD>

	      <h:selectOneListbox id="appleQuantity">

		<f:selectItem  value="0" itemLabel="0"/>
		<f:selectItem  value="1" itemLabel="1"/>
		<f:selectItem  value="2" itemLabel="2"/>
		<f:selectItem  value="3" itemLabel="3"/>
		<f:selectItem  value="4" itemLabel="4"/>
		<f:selectItem  value="5" itemLabel="5"/>
		<f:selectItem  value="6" itemLabel="6"/>
		<f:selectItem  value="7" itemLabel="7"/>
		<f:selectItem  value="8" itemLabel="8"/>
		<f:selectItem  value="9" itemLabel="9"/>

	      </h:selectOneListbox>

              Option List

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:selectOneRadio id="shipType">

		<f:selectItem value="nextDay" itemLabel="Next Day" />
		<f:selectItem value="nextWeek" itemLabel="Next Week" />
		<f:selectItem value="nextMonth" itemLabel="Next Month" />

              </h:selectOneRadio>

	</TD>

      </TR>

      <TR>

	<TD>
		<h:selectOneRadio id="verticalRadio" border="1">

            <f:selectItem value="nextDay" itemLabel="Next Day"/>
		<f:selectItem value="nextWeek" itemLabel="Next Week"  />
		<f:selectItem value="nextMonth" itemLabel="Next Month" />

                </h:selectOneRadio>

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:inputTextarea id="address" value="Hi There"
                                        rows="10" cols="10"/>

	</TD>

      </TR>

  <TABLE>

</h:form>
</f:view>

    </BODY>
</HTML>
