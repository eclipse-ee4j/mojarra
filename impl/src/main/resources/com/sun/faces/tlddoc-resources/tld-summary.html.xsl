<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Copyright (c) 2003, 2020 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Distribution License v. 1.0, which is available at
    http://www.eclipse.org/org/documents/edl-v10.php.

    SPDX-License-Identifier: BSD-3-Clause

-->

<!--
    Document   : tld-summary.html.xsl
    Created on : December 18, 2002, 3:46 PM
    Author     : mroth
    Description:
        Creates the TLD summary (right frame), listing the tags
        and functions that are in this particular tag library and 
        their descriptions.
-->

<xsl:stylesheet version="1.0"
    xmlns:javaee="http://java.sun.com/xml/ns/javaee" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">
    
    <xsl:output method="html" indent="yes"/>
    
    <xsl:param name="tlddoc-shortName">default</xsl:param>

    <!-- template rule matching source root element -->
    <xsl:template match="/">
      <xsl:apply-templates select="javaee:tlds/javaee:taglib"/>
    </xsl:template>
    
    <xsl:template match="javaee:taglib">
      <xsl:if test="javaee:short-name=$tlddoc-shortName">
        <xsl:variable name="tldname">
          <xsl:choose>
            <xsl:when test="javaee:display-name!=''">
              <xsl:value-of select="javaee:display-name"/>
            </xsl:when>
            <xsl:when test="javaee:short-name!=''">
              <xsl:value-of select="javaee:short-name"/>
            </xsl:when>
            <xsl:otherwise>
              Unnamed TLD
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="title">
          <xsl:value-of select="$tldname"/>
          (<xsl:value-of select="/javaee:tlds/javaee:config/javaee:window-title"/>)
        </xsl:variable>
        <html>
          <head>
            <title><xsl:value-of select="$title"/></title>
            <link rel="stylesheet" type="text/css" href="../stylesheet.css" 
                 title="styie"/>
          </head>
          <script>
            function asd()
            {
            parent.document.title="<xsl:value-of select="normalize-space($title)"/>";
            }
          </script>
          <body bgcolor="white" onload="asd();">
            <!-- =========== START OF NAVBAR =========== -->
            <a name="navbar_top"><!-- --></a>
            <table border="0" width="100%" cellpadding="1" cellspacing="0">
            <tr>
              <td COLSPAN="3" BGCOLOR="#EEEEFF" CLASS="NavBarCell1">
                <a NAME="navbar_top_firstrow"><!-- --></a>
                <table BORDER="0" CELLPADDING="0" CELLSPACING="3">
                  <tr ALIGN="center" VALIGN="top">
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    &#160;<a href="../overview-summary.html"><font CLASS="NavBarFont1"><b>Overview</b></font></a>&#160;</td>
                    <td BGCOLOR="#FFFFFF" CLASS="NavBarCell1Rev">    &#160;<font CLASS="NavBarFont1Rev">&#160;Library&#160;</font>&#160;</td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Tag&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    &#160;<a HREF="../help-doc.html"><font CLASS="NavBarFont1"><b>Help</b></font></a>&#160;</td>
                  </tr>
                </table>
              </td>
              <td ALIGN="right" VALIGN="top" ROWSPAN="3"><em>
                </em>
              </td>
            </tr>
            <tr>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                <!--&#160;PREV TAGLIB&#160;-->
                <!--&#160;NEXT TAGLIB&#160;-->
              </font></td>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                &#160;<a HREF="../index.html" TARGET="_top"><b>FRAMES</b></a>&#160;
                &#160;<a HREF="tld-summary.html" TARGET="_top"><b>NO FRAMES</b></a>&#160;
                <script>
                  <!--
                  if(window==top) {
                    document.writeln('<A HREF="alltags-noframe.html" TARGET=""><B>All Tags</B></A>');
                  }
                  //-->
                </script>
                <noscript>
                  <a HREF="../alltags-noframe.html" TARGET=""><b>All Tags</b></a>
                </noscript>
              </font></td>
            </tr>
            </table>
            <!-- =========== END OF NAVBAR =========== -->
            
            <hr/>
            <h2><xsl:value-of select="$tldname"/></h2>
            <hr/>
            <xsl:if test="(javaee:uri!='') and (javaee:short-name!='')">
              <b>Standard Syntax:</b><br/>
              <code>
                &#160;&#160;&#160;&#160;
                <xsl:choose>
                  <xsl:when test='starts-with(javaee:uri,"/WEB-INF/tags")'>
                    &lt;%@ taglib prefix="<xsl:value-of select="javaee:short-name"/>" tagdir="<xsl:value-of select="javaee:uri"/>" %&gt;<br/>
                  </xsl:when>
                  <xsl:otherwise>
                    &lt;%@ taglib prefix="<xsl:value-of select="javaee:short-name"/>" uri="<xsl:value-of select="javaee:uri"/>" %&gt;<br/>
                  </xsl:otherwise>
                </xsl:choose>
              </code>
              <br/>
              <b>XML Syntax:</b><br/>
              <code>
                &#160;&#160;&#160;&#160;
                <xsl:choose>
                  <xsl:when test='starts-with(javaee:uri,"/WEB-INF/tags")'>
                    &lt;anyxmlelement xmlns:<xsl:value-of select="javaee:short-name"/>="urn:jsptagdir:<xsl:value-of select="javaee:uri"/>" /&gt;<br/>
                  </xsl:when>
                  <xsl:when test='starts-with(javaee:uri,"/")'>
                    &lt;anyxmlelement xmlns:<xsl:value-of select="javaee:short-name"/>="urn:jsptld:<xsl:value-of select="javaee:uri"/>" /&gt;<br/>
                  </xsl:when>
                  <xsl:otherwise>
                    &lt;anyxmlelement xmlns:<xsl:value-of select="javaee:short-name"/>="<xsl:value-of select="javaee:uri"/>" /&gt;<br/>
                  </xsl:otherwise>
                </xsl:choose>
              </code>
              <hr/>
            </xsl:if>
            <xsl:choose>
              <xsl:when test="javaee:description!=''">
                <pre>
                  <xsl:value-of select="javaee:description" disable-output-escaping="yes"/>
                </pre>
              </xsl:when>
              <xsl:otherwise>
                No Description
              </xsl:otherwise>
            </xsl:choose>
            <p/>
            <table border="1" cellpadding="3" cellspacing="0" width="100%">
              <tr bgcolor="#CCCCFF" class="TableHeadingColor">
                <td colspan="2">
                  <font size="+2"><b>Tag Library Information</b></font>
                </td>
              </tr>
              <tr>
                <td>Display Name</td>
                <xsl:choose>
                  <xsl:when test="javaee:display-name!=''">
                    <td><xsl:value-of select="javaee:display-name"/></td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td><i>None</i></td>
                  </xsl:otherwise>
                </xsl:choose>
              </tr>
              <tr>
                <td>Version</td>
                <xsl:choose>
                  <xsl:when test="javaee:tlib-version!=''">
                    <td><xsl:value-of select="javaee:tlib-version"/></td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td><i>None</i></td>
                  </xsl:otherwise>
                </xsl:choose>
              </tr>
              <tr>
                <td>Short Name</td>
                <xsl:choose>
                  <xsl:when test="javaee:short-name!=''">
                    <td><xsl:value-of select="javaee:short-name"/></td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td><i>None</i></td>
                  </xsl:otherwise>
                </xsl:choose>
              </tr>
              <tr>
                <td>URI</td>
                <xsl:choose>
                  <xsl:when test="javaee:uri!=''">
                    <td><xsl:value-of select="javaee:uri"/></td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td><i>None</i></td>
                  </xsl:otherwise>
                </xsl:choose>
              </tr>
            </table>
            &#160;
            <p/>
            <!-- tags and tag files -->
            <xsl:if test="(count(javaee:tag)+count(javaee:tag-file)) > 0">
              <table border="1" cellpadding="3" cellspacing="0" width="100%">
                <tr bgcolor="#CCCCFF" class="TableHeadingColor">
                  <td colspan="2">
                    <font size="+2"><b>Tag Summary</b></font>
                  </td>
                </tr>
                <xsl:apply-templates select="javaee:tag|javaee:tag-file"/>
              </table>
              &#160;
              <p/>
            </xsl:if>
            <!-- functions -->
            <xsl:if test="count(javaee:function) > 0">
              <table border="1" cellpadding="3" cellspacing="0" width="100%">
                <tr bgcolor="#CCCCFF" class="TableHeadingColor">
                  <td colspan="3">
                    <font size="+2"><b>Function Summary</b></font>
                  </td>
                </tr>
                <xsl:apply-templates select="javaee:function"/>
              </table>
              &#160;
              <p/>
            </xsl:if>
            <!-- validators -->
            <xsl:if test="count(javaee:validator) > 0">
              <table border="1" cellpadding="3" cellspacing="0" width="100%">
                <tr bgcolor="#CCCCFF" class="TableHeadingColor">
                  <td colspan="2">
                    <font size="+2"><b>Tag Library Validator</b></font>
                  </td>
                </tr>
                <xsl:apply-templates select="javaee:validator"/>
              </table>
              &#160;
              <p/>
            </xsl:if>
            <!-- listeners -->
            <xsl:if test="count(javaee:listener) > 0">
              <table border="1" cellpadding="3" cellspacing="0" width="100%">
                <tr bgcolor="#CCCCFF" class="TableHeadingColor">
                  <td>
                    <font size="+2"><b>Listeners</b></font>
                  </td>
                </tr>
                <xsl:apply-templates select="javaee:listener"/>
              </table>
              &#160;
              <p/>
            </xsl:if>
            <!-- taglib-extensions -->

            <!-- =========== START OF NAVBAR =========== -->
            <a name="navbar_bottom"><!-- --></a>
            <table border="0" width="100%" cellpadding="1" cellspacing="0">
            <tr>
              <td COLSPAN="3" BGCOLOR="#EEEEFF" CLASS="NavBarCell1">
                <a NAME="navbar_bottom_firstrow"><!-- --></a>
                <table BORDER="0" CELLPADDING="0" CELLSPACING="3">
                  <tr ALIGN="center" VALIGN="top">
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    &#160;<a href="../overview-summary.html"><font CLASS="NavBarFont1"><b>Overview</b></font></a>&#160;</td>
                    <td BGCOLOR="#FFFFFF" CLASS="NavBarCell1Rev">    &#160;<font CLASS="NavBarFont1Rev">&#160;Library&#160;</font>&#160;</td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Tag&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    &#160;<a HREF="../help-doc.html"><font CLASS="NavBarFont1"><b>Help</b></font></a>&#160;</td>
                  </tr>
                </table>
              </td>
              <td ALIGN="right" VALIGN="top" ROWSPAN="3"><em>
                </em>
              </td>
            </tr>
            <tr>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                <!--&#160;PREV TAGLIB&#160;-->
                <!--&#160;NEXT TAGLIB&#160;-->
              </font></td>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                &#160;<a HREF="../index.html" TARGET="_top"><b>FRAMES</b></a>&#160;
                &#160;<a HREF="tld-summary.html" TARGET="_top"><b>NO FRAMES</b></a>&#160;
                <script>
                  <!--
                  if(window==top) {
                    document.writeln('<A HREF="alltags-noframe.html" TARGET=""><B>All Tags</B></A>');
                  }
                  //-->
                </script>
                <noscript>
                  <a HREF="../alltags-noframe.html" TARGET=""><b>All Tags</b></a>
                </noscript>
              </font></td>
            </tr>
            </table>
            <!-- =========== END OF NAVBAR =========== -->
            <hr/>
            <small><i>
            </i></small>
          </body>
        </html>
      </xsl:if>
    </xsl:template>
    
    <xsl:template match="javaee:tag|javaee:tag-file">
      <tr bgcolor="white" class="TableRowColor">
        <td width="15%">
          <b>
            <xsl:element name="a">
              <xsl:attribute name="href"><xsl:value-of select="javaee:name"/>.html</xsl:attribute>
              <xsl:value-of select="javaee:name"/>
            </xsl:element>
          </b>
        </td>
        <td>
          <xsl:choose>
            <xsl:when test="javaee:description!=''">
              <xsl:value-of select="javaee:description" disable-output-escaping="yes"/>
            </xsl:when>
            <xsl:otherwise>
              <i>No Description</i>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
    </xsl:template>

    <xsl:template match="javaee:function">
      <tr bgcolor="white" class="TableRowColor">
        <td width="15%" nowrap="" align="right">
          <code><xsl:value-of select='substring-before(normalize-space(javaee:function-signature)," ")'/></code>
        </td>
        <td width="15%" nowrap="">
          <code><b>
            <xsl:element name="a">
              <xsl:attribute name="href"><xsl:value-of select="javaee:name"/>.fn.html</xsl:attribute>
              <xsl:value-of select="javaee:name"/>
            </xsl:element>
            </b>( <xsl:value-of select='substring-after(normalize-space(javaee:function-signature),"(")'/>            
          </code>
        </td>
        <td>
          <xsl:choose>
            <xsl:when test="javaee:description!=''">
              <xsl:value-of select="javaee:description" disable-output-escaping="yes"/>
            </xsl:when>
            <xsl:otherwise>
              <i>No Description</i>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
    </xsl:template>
        
    <xsl:template match="javaee:validator">
      <tr valign="top" bgcolor="white" class="TableRowColor">
        <td width="15%">
          <b><xsl:value-of select="javaee:validator-class"/></b>
        </td>
        <td>
          <xsl:choose>
            <xsl:when test="javaee:description!=''">
              <xsl:value-of select="javaee:description" disable-output-escaping="yes"/>
            </xsl:when>
            <xsl:otherwise>
              <i>No Description</i>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="count(javaee:init-param)>0">
            <blockquote>
              <b>Initialization Parameters:</b><br/>
              <table border="1">
                <tr>
                  <td><b>Name</b></td>
                  <td><b>Value</b></td>
                  <td><b>Description</b></td>
                </tr>
                <xsl:apply-templates select="javaee:init-param"/>
              </table>
            </blockquote>
          </xsl:if>
        </td>
      </tr>
    </xsl:template>
    
    <xsl:template match="javaee:init-param">
      <tr valign="top">
        <td><xsl:value-of select="javaee:param-name"/></td>
        <td><xsl:value-of select="javaee:param-value"/></td>
        <td>
          <xsl:choose>
            <xsl:when test="javaee:param-description!=''">
              <xsl:value-of select="javaee:param-description"/>
            </xsl:when>
            <xsl:otherwise>
              <i>No Description</i>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
    </xsl:template>
    
    <xsl:template match="javaee:listener">
      <tr valign="top" bgcolor="white" class="TableRowColor">
        <td>
          <b><xsl:value-of select="javaee:listener-class"/></b>
        </td>
      </tr>
    </xsl:template>
    
</xsl:stylesheet> 
