<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Distribution License v. 1.0, which is available at
    http://www.eclipse.org/org/documents/edl-v10.php.

    SPDX-License-Identifier: BSD-3-Clause

-->

<!--
    Document   : overview-frame.html.xsl
    Created on : October 1, 2002, 5:37 PM
    Author     : mroth
    Description:
        Creates the overview frame (upper left corner), listing all tag 
        libraries included in this generation.
-->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:javaee="http://java.sun.com/xml/ns/javaee">
    
    <xsl:output method="html" indent="yes"/>

    <!-- template rule matching source root element -->
    <xsl:template match="/">
      <html>
        <head>
          <title>
            Overview (<xsl:value-of select="/javaee:tlds/javaee:config/javaee:window-title"/>)
          </title>
          <link rel="stylesheet" type="text/css" href="stylesheet.css" title="Style"/>
        </head>
        <script>
          function asd() {
            parent.document.title="Overview (<xsl:value-of select="normalize-space(/javaee:tlds/javaee:config/javaee:window-title)"/>)";
          }
        </script>
        <body bgcolor="white" onload="asd();">
          <table border="0" width="100%">
            <tr>
              <td nowrap="true">
                <font size="+1" class="FrameTitleFont">
                  <b><xsl:value-of select="/javaee:tlds/javaee:config/javaee:doc-title"/></b>
                </font>
              </td>
            </tr>
          </table>
          <table border="0" width="100%">
            <tr>
              <td nowrap="true">
                <font class="FrameItemFont">
                  <a href="alltags-frame.html" target="tldFrame"><xsl:text>All Tags / Functions</xsl:text></a>
                </font>
                <p/>
                <font size="+1" class="FrameHeadingFont">
                  Tag Libraries
                </font>
                <br/>
                <xsl:apply-templates select="javaee:tlds/javaee:taglib"/>
              </td>
            </tr>
          </table>
          <p/>
        </body>
      </html>
    </xsl:template>
    
    <xsl:template match="javaee:taglib">
      <font class="FrameItemFont">
        <xsl:element name="a">
          <xsl:attribute name="href"><xsl:value-of select="javaee:short-name"/>/tld-frame.html</xsl:attribute>
          <xsl:attribute name="target">tldFrame</xsl:attribute>
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
        </xsl:element>
      </font>
      <br/>
    </xsl:template>
</xsl:stylesheet> 
