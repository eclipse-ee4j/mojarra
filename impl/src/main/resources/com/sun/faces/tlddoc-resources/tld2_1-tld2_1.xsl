<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Copyright (c) 2003, 2020 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Distribution License v. 1.0, which is available at
    http://www.eclipse.org/org/documents/edl-v10.php.

    SPDX-License-Identifier: BSD-3-Clause

-->

<!--

  Identity transformation, added for flexibility.  
         
  1. Remove any tag-extension, function-extension and taglib-extension
     elements.
         
  Author: Mark Roth

-->

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:javaee="http://java.sun.com/xml/ns/javaee">               
  <xsl:output method="xml" indent="yes"/>  

  <xsl:template match="/javaee:taglib">
      <xsl:element name="taglib" namespace="http://java.sun.com/xml/ns/javaee">
          <xsl:attribute name="xsi:schemaLocation"
                         namespace="http://www.w3.org/2001/XMLSchema-instance">http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd</xsl:attribute>
          <xsl:attribute name="version">2.1</xsl:attribute>
          <xsl:apply-templates select="*"/>
      </xsl:element>      
  </xsl:template>
  
  <xsl:template match="javaee:tag-extension">
  </xsl:template>
  
  <xsl:template match="javaee:function-extension">
  </xsl:template>
  
  <xsl:template match="javaee:taglib-extension">
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="text()">
    <xsl:value-of select="normalize-space(.)" />
  </xsl:template>
  
</xsl:stylesheet>
