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

    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

    <BODY>
        <H3> JSF Basic Components Test Page </H3>

<f:view>

  <f:loadBundle basename="com.sun.faces.TestMessages" var="testMessages" />
<h:form id="basicForm" title="basicForm" styleClass="formClass"
      accept="html,wml" acceptcharset="some-charset" target="_self">

  <TABLE BORDER="1">

      <tr>
         <td>
                 <h:panelGrid id="logonPanel1" columns="2"
                    border="1" cellpadding="3" cellspacing="3"
                     summary="Grid with hardcoded data"
                     title="Grid with hardcoded data" >

                     <h:panelGroup id="formHeader2">
                        <h:outputText id="A2" escape="false" value="Logon&nbsp;"/>
                        <h:outputText id="B2" value="Form"/>
                     </h:panelGroup>

                   
                    <h:outputText id="text1" value="Username:"/>
                   
                    <h:inputText id="username1" styleClass="inputClass" value="JavaServerFaces" />

                    <h:inputText id="username2" styleClass="inputClass" value="JavaServerFaces" disabled="true" />

                    <h:outputText id="text2" styleClass="outputClass" value="Password:"/>

                    <h:inputSecret styleClass="secretClass" id="password1" />

                    <h:commandButton id="submit1" type="submit"
                        styleClass="commandClass" 
                        value="Login" >
                    </h:commandButton>

                    <h:commandButton id="reset1" type="reset" 
                        value="Reset">
                    </h:commandButton>

                </h:panelGrid>
             </td>
           </tr>

      <TR>

	<TD>

            <h:commandButton id="pushButton" type="button" style="someStyle"
                 disabled = "true" image="duke.gif">
            </h:commandButton>
	</TD>

      </TR>


      <TR>

	<TD>

            <h:commandButton id="imageOnlyButton" type="submit"
                 image="/duke.gif?title=LOGIN&color=red" rendered="true"> 
             </h:commandButton>
	</TD>

      </TR>

      <TR>

	<TD>

	      <h:commandLink id="link" styleClass="hyperlinkClass">
                <f:verbatim>link text with localized output</f:verbatim> 
                <h:outputText value="#{testMessages.euroMessage}" />
              </h:commandLink>

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:commandLink id="linkWithValue1" styleClass="hyperlinkClass"
                  value="link text" />
	</TD>

      </TR>

      <TR>

	<TD>

	      <h:commandLink id="linkWithValue2" styleClass="hyperlinkClass"
                  value="link text">
                  <h:outputText value="with some ~ text" />
              </h:commandLink>   
	</TD>

      </TR>

      <TR>

	<TD>

	      <h:commandLink id="imageLink" style="someStyle">
                <h:graphicImage url="duke.gif?title=LOGIN&color=red"/>
              </h:commandLink>

	</TD>

        <TD> 
            <h:graphicImage id="graphicImage" style="someStyle" url="/duke.gif" ismap="true" usemap="#map1" /> 
        </TD>

      </TR>

      <TR>
        <TD>
            <h:commandLink id="commandLink" styleClass="hyperlinkClass"><f:verbatim>link text</f:verbatim>
            </h:commandLink>
       </TD>
      </TR>

      <TR>
        <TD>
            <h:commandLink id="commandParamLink" styleClass="hyperlinkClass">
              <f:verbatim>link text</f:verbatim>
              <f:param id="hlParam1" name="name" value="horwat"/>
              <f:param id="hlParam2" name="value" value="password"/>
            </h:commandLink>
        </TD>
      </TR>

      <TR>
        <TD>
            <h:commandLink id="hrefLink"><f:verbatim escape="false"><img src="duke.gif"></f:verbatim></h:commandLink>
        </TD>
      </TR>

      <TR>
        <TD>
            <h:commandLink id="hrefParamLink" target="_top">
              <h:graphicImage url="duke.gif"/>
              <f:param id="hlParam3" name="name" value="horwat"/>
              <f:param id="hlParam4" name="value" value="password"/>
            </h:commandLink>
        </TD>
      </TR>

      <TR>

	<TD>

	      <h:outputLink value="test.html" id="outputLink" styleClass="hyperlinkClass" target="_blank"><f:verbatim>output link text</f:verbatim></h:outputLink>

	</TD>

      </TR>

      <TR>

	<TD>

	      <h:outputLink value="test.html"id="output_imageLink" style="position: absolute; left: 96px; top: 168px">
                <h:graphicImage url="duke.gif"/>
              </h:outputLink>

	</TD>

        <TD> 
            <h:graphicImage id="output_graphicImage" url="/duke.gif" ismap="true" usemap="#map1" /> 
        </TD>

      </TR>

      <TR>
        <TD>
            <h:outputLink value="test.html" id="output_commandLink" 
                style="position: absolute; left: 96px; top: 168px" styleClass="hyperlinkClass"><f:verbatim>link text</f:verbatim>
            </h:outputLink>
       </TD>
      </TR>

      <TR>
        <TD>
            <h:outputLink value="test.html" id="output_commandParamLink" styleClass="hyperlinkClass">
              <f:verbatim>link text</f:verbatim>
              <f:param id="hlParam7_1" name="#{null}" value="should_not_be_present"/>
              <f:param id="hlParam7" name="name" value="horwat"/>
              <f:param id="hlParam8" name="value" value="password"/>
              <f:param id="hlParam8_1" name="#{null}" value="should_not_be_present"/>
              <f:param id="hlParam8_2" name="name_2" value="#{null}"/>
            </h:outputLink>
        </TD>
      </TR>

      <TR>
        <TD>
            <h:outputLink value="test.html" id="output_hrefLink"><f:verbatim escape="false"><img src="duke.gif"></f:verbatim></h:outputLink>
        </TD>
      </TR>

      <TR>
        <TD>
            <h:outputLink value="test.html" id="output_hrefParamLink">
              <h:graphicImage url="duke.gif"/>
              <f:param id="hlParam5" name="name" value="horwat"/>
              <f:param id="hlParam6" name="value" value="password"/>
            </h:outputLink>
        </TD>
      </TR>



      <TR>

	<TD>
              
             <h:outputText id="outputText" value="Output Text" style="someStyle"/>
            
              <h:outputText id="testvisible1" value="This should not be visible"
                               rendered="false" />
              <h:graphicImage id="testvisible2" url="/duke.gif" rendered="false"  />

	</TD>

      </TR>

      <TR>

	<TD>
            
            <h:selectBooleanCheckbox  id="checkbox1" rendered="true"
                   styleClass="selectbooleanClass"/>
             <h:outputLabel id="labe11" style="position: absolute; left: 96px; top: 168px" for="checkbox1">
                 <h:outputText id="outputlabel1" value="CheckboxAndLabelWithFor"/>
              </h:outputLabel>
	</TD>
        </TR>

        <TR>
        <TD>
             <h:outputLabel id="labe13" style="position: absolute; left: 96px; top: 168px">
                <h:outputText id="outputlabel3" value="CheckboxAndLabelWithoutFor"/>
                 <h:selectBooleanCheckbox  id="checkbox3" rendered="true"
                   styleClass="selectbooleanClass"/>
              </h:outputLabel>
	</TD> 
        </TR>
        <TR>
        <TD>
             <h:outputLabel id="labe14" style="position: absolute; left: 96px; top: 168px">
                <h:selectBooleanCheckbox  id="checkbox4" rendered="true"
                   styleClass="selectbooleanClass"/>
                 <h:outputText id="outputlabel4" value="CheckboxAndLabelWithoutFor"/>
              </h:outputLabel>
	</TD>

      </TR>

      <TR>
        <TD>
             <h:outputLabel id="labe15" for="checkbox5" style="position: absolute; left: 96px; top: 168px">
                 <h:outputText id="outputlabel5" value="LabelBeforeForComponent"/>
              </h:outputLabel>
              <h:selectBooleanCheckbox  id="checkbox5" rendered="true"
                   styleClass="selectbooleanClass" />
	</TD>

      </TR>

      <TR>
        <TD>
             <h:outputLabel id="labe16" for="checkbox6" value = "LabelWithValue" />
           
              <h:selectBooleanCheckbox  id="checkbox6" rendered="true"
                   styleClass="selectbooleanClass" />
	</TD>

      </TR>
   
      <TR>
        <TD>
             <h:outputLabel id="labe17" for="checkbox7" value="LabelWithValue">
                <h:outputText id="outputlabel7" value="andsometext"/>
              </h:outputLabel>
           
              <h:selectBooleanCheckbox  id="checkbox7" rendered="true"
                   styleClass="selectbooleanClass" />
	</TD>

      </TR>

      <TR>
        <TD>
             <h:outputLabel id="labe18" for="checkbox8" escape="true" value="escape<p>this markup</p>">
                <h:outputText id="outputlabel8" value="andsometext"/>
              </h:outputLabel>
           
              <h:selectBooleanCheckbox  id="checkbox8" rendered="true"
                   styleClass="selectbooleanClass" />
	</TD>

      </TR>

      <TR>

	<TD>

	     <h:selectOneListbox styleClass="selectoneClass"
                     title="Select Quantity" style="someStyle"
                     tabindex="20" enabledClass="eclass" disabledClass="dclass">

                <f:selectItem  itemDisabled="true" itemValue="0" itemLabel="0"/>
                <f:selectItem  itemValue="1" itemLabel="1" itemDescription="First Item" />
                <f:selectItem  itemValue="2" itemLabel="2" />
                <f:selectItem  itemValue="3" itemLabel="3" />
                <f:selectItem  itemValue="4" itemLabel="4" />
                <f:selectItem  itemValue="5" itemLabel="5" />
                <f:selectItem  itemValue="6" itemLabel="6" />
                <f:selectItem  itemValue="7" itemLabel="7" />
                <f:selectItem  itemValue="8" itemLabel="8" />
                <f:selectItem  itemValue="9" itemLabel="9" />

              </h:selectOneListbox>

	</TD>

      </TR>

      <TR>

        <TD>

             <h:selectOneMenu styleClass="selectoneClass" disabled="true"
                     title="Select Quantity" style="someStyle"
                     tabindex="20" enabledClass="eclass" disabledClass="dclass">

                <f:selectItem  itemDisabled="true" itemValue="0" itemLabel="0"/>
                <f:selectItem  itemValue="&1&amp;" itemLabel="1" itemDescription="First Item" />
                <f:selectItem  itemValue="2" itemLabel="2" />
                <f:selectItem  itemValue="3" itemLabel="3" itemDisabled="true"/>
                <f:selectItem  itemValue="4" itemLabel="4" />
                <f:selectItem  itemValue="5" itemLabel="5" />
                <f:selectItem  itemValue="6" itemLabel="6" />
                <f:selectItem  itemValue="7" itemLabel="7" />
                <f:selectItem  itemValue="8" itemLabel="8" />
                <f:selectItem  itemValue="9" itemLabel="9" />

              </h:selectOneMenu>

        </TD>

      </TR>

      <TR>

	<TD>

	    <h:selectOneRadio id="shipType" layout="LINE_DIRECTION" 
                tabindex="3" disabledClass="disabledClass" 
                enabledClass="enabledClass" accesskey="A" 
                styleClass = "someStyleClass" style="someStyle" disabled="true">

                <f:selectItem itemValue="nextDay&" itemLabel="Next Day" itemDisabled="true"/>
                <f:selectItem itemValue="nextWeek" itemLabel="<Next Week>" />
                <f:selectItem itemValue="nextMonth" itemLabel="<Next Month>" escape="false"/>
                 
              </h:selectOneRadio>

	</TD>

      </TR>

      <TR>

	<TD>
            <h:selectOneRadio id="verticalRadio" disabled="true"
                                            layout="pageDirection" border="1" >

                <f:selectItem itemValue="nextDay" itemLabel="Next Day"
                                   itemDisabled="true" />
                <f:selectItem itemValue="nextWeek" itemLabel="Next Week"  
                        itemDisabled="false"/>
                <f:selectItem itemValue="nextMonth" itemLabel="Next Month" />

           </h:selectOneRadio>

	</TD>

      </TR>

      <TR>

        <TD>Float: 
        </TD>

	<TD><h:inputText value="3.1415" style="someStyle">
                <f:converter converterId="javax.faces.Float"/>
            </h:inputText>
	</TD>

      </TR>



      <TR>

        <TD>Date: 
        </TD>

	<TD><h:inputText value="July 10, 1996"
                              styleClass="inputClass">
                <f:convertDateTime dateStyle="long"/>
            </h:inputText>
	</TD>

      </TR>


      <TR>

        <TD>Disabled Date: 
        </TD>

	<TD><h:inputText id="date2" value="July 11, 1996"
                         readonly="true"
                        size="3" maxlength="20" tabindex="1" accesskey="D">
                <f:convertDateTime dateStyle="long"/>
            </h:inputText>
	</TD>

      </TR>

      <TR>

        <TD>DateTime: 
        </TD>

	<TD><h:inputText id="date3" 
                          value="Wed, Jul 10, 1996 AD at 12:31:31 PM">
                <f:convertDateTime pattern="EEE, MMM d, yyyy G 'at' hh:mm:ss a"/>
            </h:inputText>
	</TD>

      </TR>


      <tr>
          <td>
            <h:inputText id="testPattern" 
                        value="9999.98765" size="3" maxlength="20" 
                        styleClass="inputClass" 
                        tabindex="2" accesskey="D">
               <f:convertNumber pattern="####"/>
            </h:inputText>
              </td>

      </tr>

      <tr>
          <td> <h:outputText styleClass="outputClass" id="percentLabel" value="OUTPUT-PERCENT" /> </td>
              <td>
                   <h:outputText id="testPercent" value="45%">
                       <f:convertNumber type="number" pattern="#%"/>
                   </h:outputText>
              </td>
      </tr>

      <TR>

        <TD>OutputDate: 
        </TD>

	<TD><h:outputText id="date4" value="July 10, 1996"
                              styleClass="outputClass" >
                <f:convertDateTime dateStyle="long"/>
            </h:outputText>
	</TD>

      </TR>

      <TR>

        <TD>OutputDateTime: 
        </TD>

	<TD><h:outputText id="date5" 
                          value="Wed, Jul 10, 1996 AD at 12:31:31 PM">
                <f:convertDateTime pattern="EEE, MMM d, yyyy G 'at' hh:mm:ss a"/>
            </h:outputText>
	</TD>

      </TR>

      <TR>

        <TD>InputTime: 
        </TD>

	<TD><h:inputText id="date6" 
                          value="12:31:31 PM">
                <f:convertDateTime timeStyle="medium"/>
            </h:inputText>
	</TD>

      </TR>

      <TR>

        <TD>OutputTime: 
        </TD>

	<TD><h:outputText id="date7" 
                          value="12:31:31 PM">
                <f:convertDateTime timeStyle="medium"/>
            </h:outputText>
	</TD>

      </TR>

           <tr>

             <td>

               <h:inputText id="inputDate1" 
                                 value="Jan 12, 1952" 
                                 readonly="true"
                                 size="10" maxlength="20"
                                 alt="input_date medium readonly"
                                 accesskey="D" 
                               title="input_date medium readonly">
                   <f:convertDateTime dateStyle="medium"/>
                </h:inputText>


             </td>

            </tr>

           <tr>

             <td>

               <h:inputText id="inputDate2" 
                                 value="Jan 12, 1952" 
                                 alt="input_date medium"
                                  title="input_date medium">
                   <f:convertDateTime dateStyle="medium"/>
                </h:inputText>

             </td>

            </tr>

           <tr>

             <td>

               <h:inputText id="inputDate3" 
                                 value="01/12/1952" 
                                 alt="input_date short"
                                  title="input_date short">
                   
                   <f:convertDateTime dateStyle="short"/>
                </h:inputText>

             </td>

            </tr>

           <tr>

             <td>

               <h:inputText id="inputDate4"  
                                 value="January 12, 1952" 
                                 size="20" maxlength="40"
                                 alt="input_date long"
                                 accesskey="d"
                               title="input_date long">
                   <f:convertDateTime dateStyle="long"/>
                </h:inputText>


             </td>

            </tr>

<tr>
					<TD>Multi-select menu:</TD>
					<TD><h:selectManyMenu id="ManyApples" styleClass="selectmanyClass" style="someStyle" enabledClass="eclass" disabledClass="dclass">
						<f:selectItem itemValue="0" itemLabel="zero" />
						<f:selectItem itemValue="1" itemLabel="one" />
						<f:selectItem itemValue="2" itemLabel="two" />
						<f:selectItem itemValue="3" itemLabel="three" />
						<f:selectItem itemValue="4" itemLabel="four" itemDisabled="true"/>
						<f:selectItem itemValue="5" itemLabel="five" />
						<f:selectItem itemValue="6" itemLabel="six" />
						<f:selectItem itemValue="7" itemLabel="seven" />
						<f:selectItem itemValue="8" itemLabel="eight" />
						<f:selectItem itemValue="9" itemLabel="nine" />
					</h:selectManyMenu></TD>

</tr>
<td><h:inputText id="iescape" value="& a &amp;&"/></td>
<tr>
					<TD>Multi-select listbox:</TD>
					<TD><h:selectManyListbox style="someStyle" enabledClass="eclass"
                                              disabledClass="dclass" disabled="true">
						<f:selectItem itemValue="0" itemLabel="zero" />
						<f:selectItem itemValue="1" itemLabel="one" />
						<f:selectItem itemValue="2" itemLabel="two" />
						<f:selectItem itemValue="3" itemLabel="three" itemDisabled="true"/>
						<f:selectItem itemValue="4" itemLabel="four" />
						<f:selectItem itemValue="5" itemLabel="five" />
						<f:selectItem itemValue="6" itemLabel="six" />
						<f:selectItem itemValue="7" itemLabel="seven" />
						<f:selectItem itemValue="8" itemLabel="eight" />
						<f:selectItem itemValue="9" itemLabel="nine" />
					</h:selectManyListbox></TD>
</tr>

<tr>
					<TD><h:selectManyCheckbox id="ManyApples3" border="1" 
                                                tabindex="3" disabledClass="disabledClass" style="someStyle"
                                                styleClass="styleClass" enabledClass="enabledClass" accesskey="A">>
						<f:selectItem itemValue="0" itemLabel="zero" itemDisabled="true" />
						<f:selectItem itemValue="1" itemLabel="one" />
						<f:selectItem itemValue="2" itemLabel="two" />
						<f:selectItem itemValue="3" itemLabel="three" itemDisabled="false"/>
						<f:selectItem itemValue="4" itemLabel="four" />
						<f:selectItem itemValue="5" itemLabel="five" />
						<f:selectItem itemValue="6" itemLabel="six" />
						<f:selectItem itemValue="7" itemLabel="seven" />
						<f:selectItem itemValue="8" itemLabel="eight" />
						<f:selectItem itemValue="9" itemLabel="nine" />
					</h:selectManyCheckbox></TD>
</tr>

<tr>
                                        <TD><h:selectManyCheckbox id="checklistmodel"
                                               value="#{LoginBean.currentOptions}"
                                               disabledClass="disabedClass"
                                               enabledClass="enabledClass"
                                               disabled="true">
						<f:selectItem itemValue="1" itemLabel="<one>" />
						<f:selectItem itemValue="2" itemLabel="<two>" escape="false"/>
						<f:selectItem itemValue="3" itemLabel="three"/>
                                            </h:selectManyCheckbox></TD>
</tr>

<tr>
<td>
<h:panelGroup style="color:red" styleClass="walleye">
  <f:verbatim>style this text like a red walleye</f:verbatim>
</h:panelGroup>
</td>
</tr>

<tr>
<td>
<h:panelGroup layout="block" style="color:red" styleClass="walleye">
  <f:verbatim>style this text like a red walleye</f:verbatim>
</h:panelGroup>
</td>
</tr>

<h:inputHidden value="48%" >
    <f:convertNumber type="number" pattern="#%"/>
</h:inputHidden>

<tr><td>

<f:verbatim escape="true">1. You should see the <i>angle brackets</i> on this
text</f:verbatim>

</td>
</tr>

<tr><td>

<f:verbatim>2. You should not see the <i>angle brackets</i> on this
text</f:verbatim>

</td>
</tr>

<tr><td>

<f:verbatim escape="false">3. You should not see the <i>angle
brackets</i> on this text</f:verbatim>

</td>
</tr>

<tr><td>

<f:verbatim escape="#{SimpleBean.trueValue}">4. You should see the <i>angle
brackets</i> on this text</f:verbatim>

</td>
</tr>
<tr><td>

<f:verbatim escape="#{SimpleBean.falseValue}">5. You should not see the
<i>angle brackets</i> on this text</f:verbatim>

</td>
</tr>

<tr><td>

<h:outputText escape="true" value="6. You should see the <i>angle
brackets</i> on this text"></h:outputText>

</td>
</tr>

<tr><td>

<h:outputText id="value7" value="7. & You should see the <i>angle brackets</i> on
this text"></h:outputText>

</td>
</tr>

<tr><td>

<h:outputText escape="false" value="8. You should not see the <i>angle
brackets</i> on this text"></h:outputText>

</td>
</tr>

<tr><td>

<h:outputText escape="#{SimpleBean.trueValue}" value="9. You should see
the <i>angle brackets</i> on this text"></h:outputText>

</td>
</tr>

<tr><td>

<h:outputText escape="#{SimpleBean.falseValue}" value="10. You should not
see the <i>angle brackets</i> on this text"></h:outputText>

</td>
</tr>

<tr><td>

<h:panelGrid>
<f:subview id="subview1" rendered="false">
   <h:outputText value="This should not be rendered" />
</f:subview>
</h:panelGrid>

</td></tr>

<tr><td>

<h:panelGrid>
<h:panelGroup rendered="false">
   <h:outputText value="This should not be rendered" />
</h:panelGroup>
</h:panelGrid>

</td></tr>

<tr><td>

<h:dataTable>
<f:subview id="subview2" rendered="false">
   <h:outputText value="This should not be rendered" />
</f:subview>
</h:dataTable>


<h:outputLink value="test.html" rendered="false">
    <h:graphicImage url="duke.gif"/>
</h:outputLink>

<h:commandLink rendered="false">
      <h:outputText value="This should not be rendered" />
</h:commandLink>

<h:outputText title="output text with title" value="should be spanned" />

<h:outputFormat id="userMsg" value="Param 0: {0}" >
   <f:param value="my param"/>
</h:outputFormat>

</td></tr>

<tr><td>

<h:dataTable headerClass="table-header" footerClass="table-footer"
   rows="1">
   <f:facet             name="header">
     <h:outputText    value="Overall Table Header"/>
   </f:facet>
                                                                               
   <f:facet             name="footer">
     <h:outputText    value="Overall Table Footer"/>
   </f:facet>

   <h:column headerClass="#{SimpleBean.headerClass}" footerClass="#{SimpleBean.footerClass}">
      <f:facet           name="header">
        <h:outputText  value="Account Id"/>
      </f:facet>
      <f:facet           name="footer">
        <h:outputText  value="A.I. Footer"/>
      </f:facet>
   </h:column>

   <h:column>
      <f:facet           name="header">
        <h:outputText  value="Customer Name"/>
      </f:facet>
      <f:facet           name="footer">
        <h:outputText  value="C.N. Footer"/>
      </f:facet>
   </h:column>
</h:dataTable>

</td></tr>

</table>

</h:form>

<h:form id="form2">
  <h:commandButton value="form2 button1"/> <p />
  <h:commandButton value="form2 button2"/> <p />
</h:form>

<h:form id="form3">
  <h:commandButton value="form3 button1"/> <p />
  <h:commandLink value="form3 link1"/> <p />
</h:form>

<h:form id="form4">
  <h:outputLink value="test.html">
      Link Enabled
  </h:outputLink> 
  <h:outputLink disabled="true">
      Link Disabled
  </h:outputLink> 
  <h:outputLink disabled="true" styleClass="myClass">
      Link Disabled With Class
  </h:outputLink> 
  <h:commandLink disabled="true">
      Command Link Disabled
  </h:commandLink> 
  <h:commandLink disabled="true" styleClass="myClass">
      Command Link Disabled with Class
  </h:commandLink> 
</h:form>

</f:view>

    </BODY>
</HTML>
