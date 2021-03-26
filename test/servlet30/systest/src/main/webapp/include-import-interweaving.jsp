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

<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head> <title>Hello</title> </head>
    <%@ page contentType="application/xhtml+xml" autoFlush="true"%>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>    
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <body bgcolor="white">
    <f:view>
    <h:form id="form">
    
        <p>Outer Template Text followed by 
        <h:outputText id="outerComponent" value="outer component" /></p>
	
<table border="1">	

<tr><td>
<ul><font color="blue">        

 <f:subview id="subview01">
	    <p>Subview on the outside only with jsp:include:</p>
            <jsp:include page="TCCI_subview01.jsp" />
 </f:subview>
        
</font></ul>        

	    
</td></tr>

<tr><td>
<ul><font color="green">        

	    <p>Subview on the inside only with jsp:include:</p>
            <jsp:include page="TCCI_subview02.jsp" />
        
</font></ul>        

	    
</td></tr>

<tr><td>
<ul><font color="purple">        

	    <p>Subview on the outside and inside with jsp:include:</p>
<f:subview id="subview03">
	    <p>Template Text before the include</p>
            <jsp:include page="TCCI_subview03.jsp" />
</f:subview>
        
</font></ul>        

	    
</td></tr>

<tr><td>
<ul><font color="red">        
        <f:subview id="outerSubview3">
	    <p>Subview on the outside only with c:import:</p>
            <c:import url="TCCI_subview04.jsp" />
        </f:subview>
</font></ul>        

	    
</td></tr>


<tr><td>
<ul><font color="red">        
	    <p>Subview on the inside only with c:import:</p>
            <c:import url="TCCI_subview05.jsp" />
</font></ul>        

	    
</td></tr>

<tr><td>
<ul><font color="orange">        
	    <p>Subview on the outside and inside with c:import:</p>
<f:subview id="subview05">
	    <p>Template Text on the outside</p>
            <c:import url="TCCI_subview06.jsp" />
</f:subview>
</font></ul>        

	    
</td></tr>


</table>
	 

    </h:form>
    </f:view>
    </body>
</html>  
