/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.generate;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sun.faces.config.beans.AttributeBean;
import com.sun.faces.config.beans.DescriptionBean;
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.RenderKitBean;
import com.sun.faces.config.beans.RendererBean;


/**
 * <p>Generate javadoc style documenation about the render-kits defined in a
 * faces-config.xml file.</p>
 */

public class RenderKitSpecificationGenerator implements Generator {

    public static String DOCTYPE =
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"\"http://www.w3.org/TR/REC-html40/loose.dtd\">";


    // -------------------------------------------------------- Static Variables


    // The directory into which the HTML will be generated
    private File baseDirectory;

    // The directory into which the individual Renderer HTML will be generated
    private File renderKitDirectory;

    private String renderKitId;

    private FacesConfigBean configBean;



    // ------------------------------------------------------------ Constructors


    public RenderKitSpecificationGenerator(PropertyManager propManager) {

        this.renderKitId =
            propManager.getProperty(PropertyManager.RENDERKIT_ID);

        baseDirectory =
            new File(System.getProperty("user.dir") +
            File.separatorChar +
            propManager.getProperty(PropertyManager.BASE_OUTPUT_DIR) +
            File.separatorChar + "facesdoc");

        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }

        renderKitDirectory = new File(baseDirectory, renderKitId);

        if (!renderKitDirectory.exists()) {
            renderKitDirectory.mkdirs();
        }



    } // END RenderKitSpecificationGenerator


    // ---------------------------------------------------------- Public Methods


    @Override
    public void generate(FacesConfigBean configBean) {

        this.configBean = configBean;

        try {
            // copy the static files to the output area
            copyResourceToFile("com/sun/faces/generate/facesdoc/index.html",
                new File(baseDirectory, "index.html"));
            copyResourceToFile("com/sun/faces/generate/facesdoc/stylesheet.css",
                new File(baseDirectory, "stylesheet.css"));

            generateAllRenderersFrame();
            generateRenderKitSummary();
            generateRenderersDocs();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    } // END generate


    // --------------------------------------------------------- Private Methods


    private static String getFirstSentance(String para) throws Exception {
        int dot = para.indexOf('.');
        return para.substring(0, dot + 1);
    }


    private static void copyResourceToFile(String resourceName, File file)
        throws Exception {

        byte[] bytes = new byte[1024];

        FileOutputStream fos = new FileOutputStream(file);
        URL url = getCurrentLoader(fos).getResource(resourceName);
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        BufferedInputStream bis =
            new BufferedInputStream(conn.getInputStream());
        for (int len = bis.read(bytes, 0, 1024); len != -1;
             len = bis.read(bytes, 0, 1024)) {
            fos.write(bytes, 0, len);
        }
        fos.close();
        bis.close();
    }


    private static void writeStringToFile(String toWrite,
                                         File file) throws Exception {

        FileOutputStream fos = new FileOutputStream(file);
        byte[] bytes = toWrite.getBytes();
        fos.write(bytes);
        fos.close();
    }


    private static void appendResourceToStringBuffer(String resourceName,
                                                     StringBuffer sb)
    throws Exception {

        char[] chars = new char[1024];

        URL url = getCurrentLoader(sb).getResource(resourceName);
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        InputStreamReader isr = new InputStreamReader(conn.getInputStream());
        for (int len = isr.read(chars, 0, 1024); len != -1;
             len = isr.read(chars, 0, 1024)) {
            sb.append(chars, 0, len);
        }
        isr.close();
    }


    private void generateAllRenderersFrame() throws Exception {

        // generate the allrenderers-frame.html
        StringBuffer sb = new StringBuffer(2048);
        appendResourceToStringBuffer(
            "com/sun/faces/generate/facesdoc/allrenderers-frame.top",
            sb);
        sb.append("<FONT size=\"+1\" CLASS=\"FrameHeadingFont\">\n");

        sb.append("<B>" + renderKitId + " RenderKit ");
        String implVersionNumber = System.getProperty("impl.version.number");
        if (null != implVersionNumber) {
            sb.append("(" + implVersionNumber + ")");
        }
        sb.append("</B></FONT>\n");

        sb.append("<BR>\n\n");
        sb.append("<DL CLASS=\"FrameItemFont\">\n\n");

        Map<String, ArrayList<RendererBean>> renderersByComponentFamily =
            GeneratorUtil.getComponentFamilyRendererMap(configBean, renderKitId);

	for (Map.Entry entry : renderersByComponentFamily.entrySet()) {

            String curFamily = (String)entry.getKey();
            sb.append("  <DT>" + curFamily + "</DT>\n");
            List<RendererBean> renderers = (List<RendererBean>)entry.getValue();

            for (Iterator<RendererBean> rendererIter = renderers.iterator();
                 rendererIter.hasNext(); ) {

                RendererBean renderer = rendererIter.next();
                String curType = renderer.getRendererType();
                DescriptionBean[] descriptions = renderer.getDescriptions();
                String
                        enclosingDiv = null,
                        enclosingSpan = null;
                int [] divStart = new int[1];
                int [] spanStart = new int[1];
                if (null != descriptions) {
                    // Get the current operating locale
                    String localeStr = Locale.getDefault().getCountry().toLowerCase();
                    // iterate over the descriptions and try to find one that matches
                    // the country of the current locale
                    for (DescriptionBean cur : descriptions) {
                        if (null != cur.getLang() &&
                            (-1 != localeStr.indexOf(cur.getLang().toLowerCase()))) {

                            enclosingDiv =
                                   GeneratorUtil.getFirstDivFromString(renderer.getDescription(cur.getLang()).getDescription(), divStart);
                            enclosingSpan = GeneratorUtil.getFirstSpanFromString(renderer.getDescription(cur.getLang()).getDescription(), spanStart);

                            break;
                        }
                    }

                }
                if (null != enclosingDiv || null != enclosingSpan) {
                    String divOrSpan = (null != enclosingDiv ? enclosingDiv : enclosingSpan);
                    // If there is a div and a span, take which ever comes first
                    if (null != enclosingDiv && null != enclosingSpan) {
                        divOrSpan = (spanStart[0] < divStart[0] ? enclosingSpan : enclosingDiv);
                    }
                    sb.append("  <DD>" + divOrSpan);
                    sb.append("<A HREF=\"" + renderKitId + "/" +
                    curFamily + curType +
                    ".html\" TARGET=\"rendererFrame\">" + curType +
                            "</A>");
                    sb.append((null != enclosingDiv ? "</div>" : "</span>") +
                            "</DD>\n");
                }
                else {
                    sb.append("  <DD><A HREF=\"" + renderKitId + "/" +
                    curFamily + curType +
                    ".html\" TARGET=\"rendererFrame\">" + curType +
                            "</A></DD>\n");
                }
            }
        }

        sb.append("</dl>\n");

        appendResourceToStringBuffer(
            "com/sun/faces/generate/facesdoc/allrenderers-frame.bottom",
            sb);
        writeStringToFile(sb.toString(),
            new File(baseDirectory,
                "allrenderers-frame.html"));
    }


    private void generateRenderKitSummary() throws Exception {

        // generate the renderkit-summary.html
        StringBuffer sb = new StringBuffer(2048);
        appendResourceToStringBuffer(
            "com/sun/faces/generate/facesdoc/renderkit-summary.top",
            sb);
        sb.append("<H2>" + renderKitId + " RenderKit ");

        String implVersionNumber = System.getProperty("impl.version.number");
        if (null != implVersionNumber) {
            sb.append("(" + implVersionNumber + ")");
        }
        sb.append("</H2>");
        sb.append("<BR>\n\n");

        RenderKitBean renderKit = configBean.getRenderKit(renderKitId);
        if (renderKit == null) {
            RenderKitBean[] kits = configBean.getRenderKits();
            if (kits == null) {
                throw new IllegalStateException("no RenderKits");
            }

            renderKit = kits[0];
            if (renderKit == null) {
                throw new IllegalStateException("no RenderKits");
            }
        }

        DescriptionBean descBean = renderKit.getDescription("");
        String description = (null == descBean) ?
                             "" : descBean.getDescription();
        sb.append("<P>" + description + "</P>\n");
        sb.append("<P />");
        sb.append(
            "<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">");
        sb.append("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n");
        sb.append("<TD COLSPAN=\"3\"><FONT SIZE=\"+2\">\n");
        sb.append("<B>Renderer Summary</B></FONT></TD>\n");
        sb.append("\n");
        sb.append("<TR>\n");
        sb.append("<TH>component-family</TH>\n");
        sb.append("<TH>renderer-type</TH>\n");
        sb.append("<TH>description</TH>\n");
        sb.append("</TR>\n");

        Map<String,ArrayList<RendererBean>> renderersByComponentFamily =
            GeneratorUtil.getComponentFamilyRendererMap(configBean, renderKitId);

	for (Map.Entry entry : renderersByComponentFamily.entrySet()) {
            String curFamily = (String)entry.getKey();
            List<RendererBean> renderers = (List<RendererBean>) entry.getValue();

            sb.append("  <TR>\n");
            sb.append("    <TD rowspan=\"" + renderers.size() + "\">" +
                curFamily + "</TD>\n");
            for (Iterator<RendererBean> rendererIter = renderers.iterator();
                 rendererIter.hasNext(); ) {

                RendererBean renderer = rendererIter.next();
                String curType = renderer.getRendererType();
                sb.append("    <TD><A HREF=\"" + curFamily + curType +
                    ".html\" TARGET=\"rendererFrame\">" + curType +
                    "</A></TD>\n");
                descBean = renderer.getDescription("");
                description = (null == descBean) ?
                              "" : descBean.getDescription();
                sb.append("    <TD>" + getFirstSentance(description) +
                    "</TD>");
                if (rendererIter.hasNext()) {
                    sb.append("  </TR>\n");
                    sb.append("  <TR>\n");
                }
            }
            sb.append("  </TR>\n");
        }

        sb.append("</TABLE>\n\n");

        appendResourceToStringBuffer(
            "com/sun/faces/generate/facesdoc/renderkit-summary.bottom",
            sb);
        writeStringToFile(sb.toString(),
            new File(renderKitDirectory,
                "renderkit-summary.html"));
    }


    private void generateRenderersDocs() throws Exception {
        StringBuffer sb;
        RenderKitBean renderKit;
        DescriptionBean descBean;

        String description;
        String rendererType;
        String componentFamily;
        String defaultValue;
        String title;

        // generate the docus for each renderer

        if (null == (renderKit = configBean.getRenderKit(renderKitId))) {
            RenderKitBean[] kits = configBean.getRenderKits();
            if (kits == null) {
                throw new IllegalStateException("no RenderKits");
            }

            renderKit = kits[0];
            if (renderKit == null) {
                throw new IllegalStateException("no RenderKits");
            }
        }
        RendererBean[] renderers = renderKit.getRenderers();
        AttributeBean[] attributes;
        sb = new StringBuffer(2048);

        for (int i = 0, len = renderers.length; i < len; i++) {
            if (null == renderers[i]) {
                throw new IllegalStateException("null Renderer at index: " + i);
            }
            attributes = renderers[i].getAttributes();

            sb.append(DOCTYPE + "\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            // PENDING timestamp
            sb.append("<title>\n");
            title = "<font size=\"-1\">component-family:</font> " +
                (componentFamily = renderers[i].getComponentFamily()) +
                " <font size=\"-1\">renderer-type:</font> " +
                (rendererType = renderers[i].getRendererType());
            sb.append(title + "\n");
            sb.append("</title>\n");
            // PENDING META tag
            sb.append(
                "<link REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"../stylesheet.css\" TITLE=\"Style\">\n");
            sb.append("</head>\n");
            sb.append("<script>\n");
            sb.append("function asd()\n");
            sb.append("{\n");
            sb.append("  parent.document.title=" + title + "\n");
            sb.append("}\n");
            sb.append("</SCRIPT>\n");
            sb.append("<body BGCOLOR=\"white\" onload=\"asd();\">\n");
            sb.append("\n");
            sb.append("<H2><font size=\"-1\">" + renderKitId +
                " render-kit</font>\n");
            sb.append("<br />\n");
            sb.append(title + "\n");
            sb.append("</H2>\n");
            sb.append("<HR />\n");
            descBean = renderers[i].getDescription("");
            description = (null == descBean) ? "" : descBean.getDescription();
            sb.append("<P>" + description + "</P>\n");
            // render our renders children status

            if (renderers[i].isRendersChildren()) {
                sb.append(
                    "<P>This renderer is responsible for rendering its children.</P>");
            } else {
                sb.append(
                    "<P>This renderer is not responsible for rendering its children.</P>");
            }

            // if we have attributes
            if ((null == attributes) || (0 < attributes.length)) {
                sb.append("<HR />\n");
                sb.append("<a NAME=\"attributes\"><!-- --></a>\n");
                sb.append("\n");
                sb.append("<h3>Note:</h3>\n");
                sb.append("\n");
                sb.append(
                    "<p>Attributes with a <code class=\"changed_modified_2_2\">ignored-by-renderer</code> value of\n");
                sb.append(
                    "<code>true</code> are not interpreted by the renderer and are conveyed\n");
                sb.append(
                    "straight to the rendered markup, without checking for validity.  Attributes with a\n");
                sb.append(
                    "<code class=\"changed_modified_2_2\">ignored-by-renderer</code> value of <code>false</code> are interpreted\n");
                sb.append(
                    "by the renderer, and may or may not be checked for validity by the renderer.</p>\n");
                sb.append("\n");
                sb.append(
                    "<table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n");
                sb.append(
                    "<tr BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n");
                sb.append("<td COLSPAN=\"5\"><font SIZE=\"+2\">\n");
                sb.append("<b>Attributes</b></font></td>\n");
                sb.append("</tr>\n");
                sb.append(
                    "<tr BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n");
                sb.append("<th><b>attribute-name</b></th>\n");
                sb.append("<th><b class=\"changed_modified_2_2\">ignored-by-renderer</b></th>\n");
                sb.append("<th><b>attribute-class</b></th>\n");
                sb.append("<th><b>description</b></th>\n");
                sb.append("<th><b>default-value</b></th>\n");
                sb.append("</tr>\n");
                sb.append("	    \n");
                // output each attribute
		if (attributes != null) {
                    for (int j = 0, attrLen = attributes.length; j < attrLen; j++) {
                        if (attributes[j].isAttributeIgnoredForRenderer()) {
                            continue;
                        }
                        sb.append(
                            "<tr BGCOLOR=\"white\" CLASS=\"TableRowColor\">\n");
                        sb.append(
                            "<td ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"1%\"><code>\n");
                        sb.append(
                            "&nbsp;" + attributes[j].getAttributeName() + "\n");
                        sb.append("</td>\n");
                        sb.append("<td ALIGN=\"right\" VALIGN=\"top\">" +
                            attributes[j].isPassThrough() + "</td>\n");
                        sb.append("<td><code>" + attributes[j].getAttributeClass() +
                            "</code></td>\n");
                        descBean = attributes[j].getDescription("");
                        description = (null == descBean) ?
                                      "" : descBean.getDescription();
                        sb.append("<td>" + description + "</td>\n");
                        if (null ==
                            (defaultValue = attributes[j].getDefaultValue())) {
                            defaultValue = "undefined";
                        }
                        sb.append("<td>" + defaultValue + "<td>\n");
                        sb.append("</tr>\n");
                    }
		}
                sb.append("</table>\n");
            } else {
                sb.append("<p>This renderer-type has no attributes</p>\n");
            }
            sb.append("<hr>\n");
            sb.append(
                "Copyright (c) 2003-2017 Oracle America, Inc. All Rights Reserved.\n");
            sb.append("</body>\n");
            sb.append("</html>\n");
            writeStringToFile(sb.toString(),
                new File(renderKitDirectory,
                    componentFamily + rendererType +
                ".html"));
            sb.delete(0, sb.length());
        }
    }


    private static ClassLoader getCurrentLoader(Object fallbackClass) {
        ClassLoader loader =
            Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }

    // ------------------------------------------------------------- Main Method


    public static void main(String[] args) throws Exception {
        PropertyManager propManager = PropertyManager.newInstance(args[0]);

        Generator generator =
            new RenderKitSpecificationGenerator(propManager);

        generator.generate(GeneratorUtil.getConfigBean(args[1]));

    }


}
