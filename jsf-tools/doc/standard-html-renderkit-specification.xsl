<?xml version="1.0" encoding="ISO-8859-1"?>
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

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html"/>

<xsl:template match="/">
<html>

<head>

<title>JavaServer Faces 1.0 Standard HTML RenderKit Specification</title>

<STYLE TYPE="text/css" MEDIA="screen">

<xsl:comment>

  TABLE  { border-width: medium; border-style: groove }

  TD  { border-width: thin; border-style: groove }

  TH { background-color: #dcdcdc }

</xsl:comment>

</STYLE>

</head>

<!-- Note that you need to modify the standard-html-renderkit.xml file
to make it so that render-kit is the root XML element in order for this
XSL file to work.  -->

<body>

  <h2>Passthru attributes</h2>

  <p>The following attributes are passed thru without interpretation
  directly to the rendered markup.</p>

  <table>

    <tr>

      <th>attribute</th>

      <th>renderers supporting that attribute</th>

    </tr>

    <tr>

      <td>disabled</td>

    </tr>

    <tr>

      <td>readonly</td>

    </tr>

    <tr>

      <td>ismap</td>

    </tr>

    <tr>

      <td>accesskey</td>

    </tr>

    <tr>

      <td>alt</td>

    </tr>

    <tr>

      <td>cols</td>

    </tr>

    <tr>

      <td>height</td>

    </tr>

    <tr>

      <td>lang</td>

    </tr>

    <tr>

      <td>longdesc</td>

    </tr>

    <tr>

      <td>maxlength</td>

    </tr>

    <tr>

      <td>onblur</td>

    </tr>

    <tr>

      <td>onchange</td>

    </tr>

    <tr>

      <td>onclick</td>

    </tr>

    <tr>

      <td>ondblclick</td>

    </tr>

    <tr>

      <td>onfocus</td>

    </tr>

    <tr>

      <td>onkeydown</td>

    </tr>

    <tr>

      <td>onkeypress</td>

    </tr>

    <tr>

      <td>onkeyup</td>

    </tr>

    <tr>

      <td>onload</td>

    </tr>

    <tr>

      <td>onmousedown</td>

    </tr>

    <tr>

      <td>onmousemove</td>

    </tr>

    <tr>

      <td>onmouseout</td>

    </tr>

    <tr>

      <td>onmouseover</td>

    </tr>

    <tr>

      <td>onmouseup</td>

    </tr>

    <tr>

      <td>onreset</td>

    </tr>

    <tr>

      <td>onselect</td>

    </tr>

    <tr>

      <td>onsubmit</td>

    </tr>

    <tr>

      <td>onunload</td>

    </tr>

    <tr>

      <td>rows</td>

    </tr>

    <tr>

      <td>size</td>

    </tr>

    <tr>

      <td>tabindex</td>

    </tr>

    <tr>

      <td>title</td>

    </tr>

    <tr>

      <td>style</td>

    </tr>

    <tr>

      <td>width</td>

    </tr>

    <tr>

      <td>dir</td>

    </tr>

    <tr>

      <td>rules</td>

    </tr>

    <tr>

      <td>frame</td>

    </tr>

    <tr>

      <td>border</td>

    </tr>

    <tr>

      <td>cellspacing</td>

    </tr>

    <tr>

      <td>cellpadding</td>

    </tr>

    <tr>

      <td>summary</td>

    </tr>

    <tr>

      <td>bgcolor</td>

    </tr>

    <tr>

      <td>usemap</td>

    </tr>

    <tr>

      <td>enctype </td>

    </tr>

    <tr>

      <td>acceptcharset </td>

    </tr>

    <tr>

      <td>accept </td>

    </tr>

    <tr>

      <td>target </td>

    </tr>

    <tr>

      <td>onreset</td>

    </tr>

    <tr>

      <td>rel</td>

    </tr>

    <tr>

      <td>rev</td>

    </tr>

    <tr>

      <td>shape</td>

    </tr>

    <tr>

      <td>coords</td>

    </tr>

    <tr>

      <td>hreflang</td>

    </tr>

  </table>

  <h2>Renderer Descriptions</h2>

  <table>

    <tr>

      <th>renderer-type</th>

      <th>description</th>

    </tr>

    <xsl:for-each select="render-kit/renderer">

      <tr>

	<td><xsl:value-of select="renderer-type"/></td>

	<td><xsl:value-of select="description"/></td>

      </tr>

    </xsl:for-each>

  </table>

</body>

</html>

</xsl:template>

</xsl:stylesheet>
