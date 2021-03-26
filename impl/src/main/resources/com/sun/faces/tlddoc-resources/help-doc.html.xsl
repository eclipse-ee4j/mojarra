<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Copyright (c) 2003, 2020 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Distribution License v. 1.0, which is available at
    http://www.eclipse.org/org/documents/edl-v10.php.

    SPDX-License-Identifier: BSD-3-Clause

-->

<!--
    Document   : help-doc.html.xsl
    Created on : October 2, 2002, 5:37 PM
    Author     : mroth
    Description:
        Creates the help-doc page for Tag Library Documentation Generator
-->

<xsl:stylesheet version="1.0"
    xmlns:javaee="http://java.sun.com/xml/ns/javaee" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">
    
    <xsl:output method="html" indent="yes"/>

    <!-- template rule matching source root element -->
    <xsl:template match="/">
      <HTML>
        <HEAD>
          <TITLE>
            API Help (<xsl:value-of select="/javaee:tlds/javaee:config/javaee:window-title"/>)
          </TITLE>
          <LINK REL ="stylesheet" TYPE="text/css" HREF="stylesheet.css" TITLE="Style"/>
        </HEAD>
        <SCRIPT>
          function asd() {
            parent.document.title="API Help (<xsl:value-of select="normalize-space(/javaee:tlds/javaee:config/javaee:window-title)"/>)";
          }
        </SCRIPT>
        <BODY BGCOLOR="white" onload="asd();">
          <a name="navbar_top"><!-- --></a>
          <table border="0" width="100%" cellpadding="1" cellspacing="0">
            <tr>
              <td COLSPAN="3" BGCOLOR="#EEEEFF" CLASS="NavBarCell1">
                <a NAME="navbar_top_firstrow"><!-- --></a>
                <table BORDER="0" CELLPADDING="0" CELLSPACING="3">
                  <tr ALIGN="center" VALIGN="top">
                    <td BGCOLOR="#FFFFFF" CLASS="NavBarCell1"> &#160;<a href="overview-summary.html"><font CLASS="NavBarFont1"><b>Overview</b></font></a>&#160;</td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Library&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Tag&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1Rev">    &#160;<font CLASS="NavBarFont1Rev"><b>Help</b></font>&#160;</td>
                  </tr>
                </table>
              </td>
              <td ALIGN="right" VALIGN="top" ROWSPAN="3"><em>
                </em>
              </td>
            </tr>
            <tr>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                &#160;PREV&#160;
                &#160;NEXT&#160;
              </font></td>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                &#160;<a HREF="index.html" TARGET="_top"><b>FRAMES</b></a>&#160;
                &#160;<a HREF="help-doc.html" TARGET="_top"><b>NO FRAMES</b></a>&#160;
                <script>
                  <!--
                  if(window==top) {
                    document.writeln('<A HREF="alltags-noframe.html" TARGET=""><B>All Tags</B></A>');
                  }
                  //-->
                </script>
                <noscript>
                  <a HREF="alltags-noframe.html" TARGET=""><b>All Tags</b></a>
                </noscript>
              </font></td>
            </tr>
          </table>
          <HR/>
          <CENTER>
            <H1>How This Tag Library Document Is Organized</H1>
          </CENTER>
          This TLD (Tag Library Descriptor) document has pages corresponding 
          to the items in the navigation bar, described as follows.
          <H3>Overview</H3>
          <BLOCKQUOTE>
            <P/>
            The <A HREF="overview-summary.html">Overview</A> page is the front 
            page of this TLD document and provides a list of all tag libraries
            with a summary for each.
          </BLOCKQUOTE>
          <H3>Library</H3>
          <BLOCKQUOTE>
            <P/>
            Each tag library has a page that contains a list of its 
            validator, listeners, tags, and functions, with a summary for each.
            This page can contain four categories:
            <UL>
              <li>Validator</li>
              <li>Listeners</li>
              <li>Tags</li>
              <li>Functions</li>
            </UL>
          </BLOCKQUOTE>
          <H3>Validator</H3>
          <BLOCKQUOTE>
            <P/>
            A tag library can have at most one validator.  If a tag library
            has a validator, it has its own page describing the validator,
            the class that implements the validator, and the available
            initialization parameters.
          </BLOCKQUOTE>
          <h3>Listeners</h3>
          <blockquote>
            <p/>
            A tag library can have zero or more listeners.  If a tag library
            has at least one listener, a page is generated that lists all
            listener classes registered for the tag library.
          </blockquote>
          <h3>Tags</h3>
          <blockquote>
            <p/>
            A tag library can have zero or more tags.  Each tag has its own
            page that describes the tag, its display name, its unique action
            name, the class that implements the tag, the TagExtraInfo class,
            the body content type, scripting variable information, attributes,
            whether the tag supports dynamic attributes, and an optional 
            example use of the tag.
          </blockquote>
          <h3>Functions</h3>
          <blockquote>
            <p/>
            A tag library can contain zero or more EL functions.  If a tag
            library has at least one function, a page is generated that lists
            all functions, the class that implements the function, the
            function signature, and an optional example use of the function.
          </blockquote>
          <!--
          <H3>Index</H3>
          <BLOCKQUOTE>
            The <A HREF="index-files/index-1.html">Index</A> contains an 
            alphabetic list of all validators, listeners, tags, functions, 
            variables, and attributes.
          </BLOCKQUOTE>
          -->
          <H3>Prev/Next</H3>
          <blockquote>
            These links take you to the next or previous validator, listener, 
            tag, function, or related page.
          </blockquote>
          <H3>Frames/No Frames</H3>
          <blockquote>
            These links show and hide the HTML frames.  All pages are available 
            with or without frames.
          </blockquote>
          <BR/>
          <HR/>
          <a name="navbar_bottom"><!-- --></a>
          <table border="0" width="100%" cellpadding="1" cellspacing="0">
            <tr>
              <td COLSPAN="3" BGCOLOR="#EEEEFF" CLASS="NavBarCell1">
                <a NAME="navbar_bottom_firstrow"><!-- --></a>
                <table BORDER="0" CELLPADDING="0" CELLSPACING="3">
                  <tr ALIGN="center" VALIGN="top">
                    <td BGCOLOR="#FFFFFF" CLASS="NavBarCell1"> &#160;<a href="overview-summary.html"><font CLASS="NavBarFont1"><b>Overview</b></font></a>&#160;</td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Library&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1">    <font CLASS="NavBarFont1">&#160;Tag&#160;</font></td>
                    <td BGCOLOR="#EEEEFF" CLASS="NavBarCell1Rev">    &#160;<font CLASS="NavBarFont1Rev"><b>Help</b></font>&#160;</td>
                  </tr>
                </table>
              </td>
              <td ALIGN="right" VALIGN="top" ROWSPAN="3"><em>
                </em>
              </td>
            </tr>
            <tr>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                &#160;PREV&#160;
                &#160;NEXT&#160;
              </font></td>
              <td BGCOLOR="white" CLASS="NavBarCell2"><font SIZE="-2">
                &#160;<a HREF="index.html" TARGET="_top"><b>FRAMES</b></a>&#160;
                &#160;<a HREF="help-doc.html" TARGET="_top"><b>NO FRAMES</b></a>&#160;
                <script>
                  <!--
                  if(window==top) {
                    document.writeln('<A HREF="alltags-noframe.html" TARGET=""><B>All Tags</B></A>');
                  }
                  //-->
                </script>
                <noscript>
                  <a HREF="alltags-noframe.html" TARGET=""><b>All Tags</b></a>
                </noscript>
              </font></td>
            </tr>
          </table>
          <HR/>
          <small><i>
          Output Generated by 
          <a href="http://taglibrarydoc.dev.java.net/" target="_blank">Tag Library Documentation Generator</a>.
          </i></small>
        </BODY>
      </HTML>
    </xsl:template>
</xsl:stylesheet> 
