<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

-->

<!--

  Translates a JSF 1.0/1.1 faces-config document into a Faces 3.0 faces-config
  document, using the following conversion rules:

  1. Change the <facelet-taglib> element to read as follows:
     <taglib xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
         http://java.sun.com/xml/ns/javaee/web-facelettaglibrary_2_2.xsd">
  2. Change the namespace of all elements to the default of
     http://java.sun.com/xml/ns/javaee

-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:old="http://java.sun.com/JSF/Facelet">
    <xsl:output method="xml" />
    
    <xsl:template match="/old:facelet-taglib">
        <xsl:element name="facelet-taglib" namespace="http://java.sun.com/xml/ns/javaee">
            <xsl:attribute name="xsi:schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">https://jakarta.ee/xml/ns/jakartaee/ https://jakarta.ee/xml/ns/jakartaee/web-facelettaglibrary_3_0.xsd</xsl:attribute>
            <xsl:attribute name="version">3.0</xsl:attribute>
            <xsl:apply-templates select="*" />
        </xsl:element>
    </xsl:template>

    <!-- Convert all 1.0/1.1 elements to 1.2 -->
    <xsl:template match="old:*">
        <xsl:element name="{local-name()}" namespace="http://java.sun.com/xml/ns/javaee">
            <xsl:copy-of select="@*" />
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
