<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Copyright (c) 2003, 2020 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Distribution License v. 1.0, which is available at
    http://www.eclipse.org/org/documents/edl-v10.php.

    SPDX-License-Identifier: BSD-3-Clause

-->

<!--
    Document   : index.html.xsl
    Created on : October 1, 2002, 5:37 PM
    Author     : mroth
    Description:
        Creates the index page for Tag Library Documentation Generator
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
          <title>
            <xsl:value-of select="/javaee:tlds/javaee:config/javaee:window-title"/>
          </title>
        </head>
        <frameset cols="20%,80%">
          <frameset rows="30%,70%">
            <frame src="overview-frame.html" name="tldListFrame"/>
            <frame src="alltags-frame.html" name="tldFrame"/>
          </frameset>
          <frame src="overview-summary.html" name="tagFrame"/>
        </frameset>
        <noframes>
          <h2>Frame Alert</h2>
          <p/>
          This document is designed to be viewed using the frames feature.  
          If you see this message, you are using a non-frame-capable web 
          client.
          <br/>
          Link to <a href="overview-summary.html">Non-frame version.</a>
        </noframes>
      </html>
    </xsl:template>
</xsl:stylesheet> 
