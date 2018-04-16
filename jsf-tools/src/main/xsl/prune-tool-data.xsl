<?xml version="1.0"?>
<!--

    Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.

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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:jsf="http://java.sun.com/xml/ns/javaee"
                version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:strip-space elements="*"/>
    <xsl:namespace-alias stylesheet-prefix="jsf" result-prefix="#default"/>

    <xsl:template match="jsf:attribute"/>
    <xsl:template match="jsf:component-extension"/>
    <xsl:template match="jsf:description"/>
    <xsl:template match="jsf:display-name"/>
    <xsl:template match="jsf:facet"/>
    <xsl:template match="jsf:large-icon"/>
    <xsl:template match="jsf:property"/>
    <xsl:template match="jsf:renderer-extension"/>
    <xsl:template match="jsf:small-icon"/>
    <xsl:template match="*|@*|text()">
       <xsl:copy>
           <xsl:apply-templates select="*|@*|text()"/>
       </xsl:copy> 
    </xsl:template>
</xsl:stylesheet>
