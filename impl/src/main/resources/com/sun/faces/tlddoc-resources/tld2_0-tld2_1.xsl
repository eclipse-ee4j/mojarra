<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Copyright (c) 2003, 2020 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Distribution License v. 1.0, which is available at
    http://www.eclipse.org/org/documents/edl-v10.php.

    SPDX-License-Identifier: BSD-3-Clause

-->

<!--

  Identity transformation (changing from the J2EE namespace
  to the Java EE namespace), added for flexibility.  

  1. Change the <taglib> element to read as follows:
     <taglib xmlns="http://java.sun.com/xml/ns/j2ee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee
         http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd">                    
         
  Author: Mark Roth

-->

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:j2ee="http://java.sun.com/xml/ns/j2ee">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/j2ee:taglib">
    <xsl:element name="taglib" namespace="http://java.sun.com/xml/ns/javaee">
      <xsl:attribute name="xsi:schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd</xsl:attribute>
      <xsl:attribute name="version">2.1</xsl:attribute>
      <xsl:apply-templates select="*"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="j2ee:*">
    <xsl:element name="{local-name()}" namespace="http://java.sun.com/xml/ns/javaee">
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
    
</xsl:stylesheet>
