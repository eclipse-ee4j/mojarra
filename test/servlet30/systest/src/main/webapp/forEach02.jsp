<%--

    Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.

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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
  <head>
    <title></title>
  </head>

  <body>
    <h1></h1>

<f:view>

<h:messages/>
<br>

<h:form               id="form">


   <table border="1" style="list-background"
        summary="Add books from the catalog to your shopping cart.">
       <tr><th>header</th>
       </tr>

       <c:forEach items="#{BooksBean.books}" var="book" varStatus="stat" >
          <tr styleClass="${(stat.index % 2) == 0 ? "list-row-event" : "list-row-odd"}">
               <td style="list-column-left">
               <h:commandLink action="null">
                       
                          <h:outputText id="bookTitle" value="#{book.title}"/>
                       
               </h:commandLink>
               </td>
          </tr>
       </c:forEach>
  </table>

</h:form>

</f:view>



    <hr>
    <address><a href="mailto:ed.burns@sun.com">Edward Burns</a></address>


Last modified: Tue Oct  5 01:38:02 EDT 2010
<!-- hhmts end -->
  </body>
</html>
