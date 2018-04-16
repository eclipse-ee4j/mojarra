<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Distribution License v. 1.0, which is available at
    http://www.eclipse.org/org/documents/edl-v10.php.

    SPDX-License-Identifier: BSD-3-Clause

-->

<!--
    Document   : alltags-frame.html.xsl
    Created on : October 1, 2002, 5:37 PM
    Author     : mroth
    Description:
        Creates the all tags page, listing all tags
        and functions included in all tag libraries for this generation.
-->

<xsl:stylesheet version="1.0"
    xmlns:javaee="http://java.sun.com/xml/ns/javaee" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">
    
    <xsl:output method="html" indent="yes"/>

    <!-- template rule matching source root element -->
    <xsl:template match="/">
      <html>
        <head>
          <title>All Tags / Functions</title>
          <link rel="stylesheet" type="text/css" href="stylesheet.css" title="Style"/>
        </head>
        <script>
          function asd()
          {
            parent.document.title="All Tags / Functions";
          }
        </script>
        <body bgcolor="white" onload="asd();">
          <font size="+1" class="FrameHeadingFont">
          <b>All Tags / Functions</b></font>
          <br/>
          <table border="0" width="100%">
            <tr>
              <td nowrap="true"><font class="FrameItemFont">
                <xsl:apply-templates 
                    select="javaee:tlds/javaee:taglib/javaee:tag|javaee:tlds/javaee:taglib/javaee:tag-file|javaee:tlds/javaee:taglib/javaee:function">
                  <xsl:sort select="../javaee:short-name"/>
                  <xsl:sort select="javaee:name"/>
                </xsl:apply-templates>
              </font></td>
            </tr>
          </table>
        </body>
      </html>    
    </xsl:template>
    
    <xsl:template match="javaee:tag|javaee:tag-file">
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="../javaee:short-name"/>/<xsl:value-of select="javaee:name"/>.html</xsl:attribute>
        <xsl:attribute name="target"></xsl:attribute>
        <xsl:value-of select="../javaee:short-name"/>:<xsl:value-of select="javaee:name"/>
      </xsl:element>
      <br/>
    </xsl:template>
    
    <!-- 
      - Same as above, but add the () to indicate it's a function 
      - and change the HTML to .fn.html
      -->
    <xsl:template match="javaee:function">
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="../javaee:short-name"/>/<xsl:value-of select="javaee:name"/>.fn.html</xsl:attribute>
        <xsl:attribute name="target">tagFrame</xsl:attribute>
        <i><xsl:value-of select="../javaee:short-name"/>:<xsl:value-of select="javaee:name"/>()</i>
      </xsl:element>
      <br/>
    </xsl:template>
</xsl:stylesheet> 
