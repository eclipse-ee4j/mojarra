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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Stack;

import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.RendererBean;

/**
 * <p>Base class for JSP TLD generation.</p>
 */
public abstract class JspTLDGenerator implements Generator {

    // Defaults
    private static final String DEFAULT_BODY_CONTENT = "JSP";
    private static final String DEFAULT_RTEXPRVALUE = "false";

    protected XMLWriter writer;
    protected FacesConfigBean configBean;
    protected PropertyManager propManager;

    private String outputFile;


    // ------------------------------------------------------------ Constructors


    public JspTLDGenerator(PropertyManager propManager) {

        this.propManager = propManager;

        File outputDir =
            new File(System.getProperty("user.dir") + File.separatorChar +
            propManager.getProperty(PropertyManager.BASE_OUTPUT_DIR) +
            File.separatorChar + "conf" + File.separatorChar + "share");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        outputFile = outputDir.toString() + File.separatorChar +
            propManager.getProperty(PropertyManager.TAGLIB_FILE_NAME);

    }


    // -------------------------------------------------- Methods from Generator


    @Override
    public void generate(FacesConfigBean configBean) {

        this.configBean = configBean;
        try {
            initWriter();
            writeDocument();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }


    } // END generate


    // ------------------------------------------------------- Protected Methods

    protected void writeDocument() throws IOException {

        writeProlog();
        writeTldDescription();
        writeValidators();
        writeTags();
        endDocument();

    } // END writeDocument


    protected void writeProlog() throws IOException {

        writer.writeProlog();

    } // END writeProlog

    protected abstract void writeTldDescription() throws IOException;

    protected abstract void writeTags() throws IOException;

    protected void endDocument() throws IOException {

        writer.closeAllElements();
        writer.flush();
        writer.close();

    } // END endDocument

    protected void writeValidators() throws IOException {

        // this one is common to both 1.2 and 2.1
        writer.writeComment(
            "============== Tag Library Validator =============");
        writer.startElement("validator");
        writer.startElement("validator-class");
        writer.writeText("com.sun.faces.taglib.html_basic.HtmlBasicValidator");
        writer.closeElement(2);

    } // END tldValidator


    protected void initWriter() throws IOException {

        writer = new XMLWriter(new FileOutputStream(outputFile));

    } // END initWriter


    // --------------------------------------------------------- Utility Methods


    /**
     * Build the tag name from componentFamily and rendererType.  The name
     * will be "camel case".
     *
     * @param componentFamily the component family
     * @param rendererType the renderer type
     * @return tldTagName
     */
    protected String makeTldTagName(String componentFamily,
                                    String rendererType) {

        if (componentFamily == null) {
            return null;
        }
        if (rendererType.startsWith("resource.")) {
            rendererType = rendererType.split("\\.")[1];
        }
        String tagName =
            Character.toLowerCase(componentFamily.charAt(0)) +
            componentFamily.substring(1);

        if (rendererType == null || componentFamily.equals(rendererType)) {
            return tagName;
        } else {
            return (tagName + rendererType);
        }

    } // END makeTldTagName


    /**
     * @param renderer renderer
     * @param  attribute attribute
     * @return true if this attribute is in the set of attributes to be excluded
     *         by this renderer.
     */
    protected boolean attributeShouldBeExcluded(RendererBean renderer,
                                                String attribute) {
        String excludedAttributes = renderer.getExcludeAttributes();
        boolean skip = false;

        if (excludedAttributes != null) {
            skip = (attribute != null &&
                excludedAttributes.indexOf(attribute) != -1);
        }

        return skip;

    } // END attributeShouldBeExcluded


    /*
     * Return the tag body content information (if any) for a given tag.
     */
    protected String getBodyContent(String tagName) {
        return DEFAULT_BODY_CONTENT;
    }

    /*
     * Return the "rtexprvalue" element value for the tag attribute.
     */
    protected String getRtexprvalue(String tagName, String attributeName) {
        return DEFAULT_RTEXPRVALUE;
    }


    /*
     * <p>Load any additional tag definitions from the specified file.  This
     * file might include tags such as "column" which have no renderer, but need
     * to be generated into the TLD file.</p>
     */
    protected String loadOptionalTags() throws IOException {

        String path =
            propManager.getProperty(PropertyManager.TAGLIB_INCLUDE);

        if (path != null) {
            if (path.charAt(0) == '/' || path.charAt(1) == ':') {
                // absolute path
                if (path.charAt(1) == ':') {
                    // win32
                    path = path.replace('/', File.separatorChar);
                }
            } else {
                path = System.getProperty("user.dir") + File.separatorChar +
                    path;
                path = path.replace('/', File.separatorChar);
            }

            StringBuffer sb = new StringBuffer();
            BufferedReader reader =
                new BufferedReader(new FileReader(path));
            int ch;
            while ((ch = reader.read()) >= 0) {
                sb.append((char) ch);
            }
            reader.close();

            return sb.toString();

        }

        return null;

    } // END loadOptionalTags


    // ---------------------------------------------------------- PrivateMethods






    // ----------------------------------------------------------- Inner Classes


    /**
     * A simple class to handle the writing of XML documents.
     */
    protected static class XMLWriter extends OutputStreamWriter {

        private static final String XML_PROLOG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";

        private static final String TAB = "    ";
        private static final int NUM_COMMENT_CHARS = 10;

        private static final String START_COMMENT = "\n<!-- ";
        private static final String END_COMMENT = " -->\n\n";

        private static final Charset UTF8 = Charset.forName("UTF-8");

        private Stack<String> elementStack = new Stack<String>();



        // -------------------------------------------------------- Constructors


        XMLWriter(OutputStream stream) {

            super(stream, UTF8);

        } // END XMLWriter


        // ----------------------------------------------------- Public Methods


        public void writeProlog() throws IOException {

            super.write(XML_PROLOG);

        } // END writeProlog


        public void writeText(String value) throws IOException {

            if (value == null) {
                // aid in detecting issues
                value = "NULL VALUE";
            }

            StringBuffer sb = new StringBuffer();
            String[] tokens = value.split("\n");
            for (int i = 0; i < tokens.length; i++) {
                String val = tokens[i].trim();
                for (int ii = 0, size = elementStack.size(); ii < size; ii++) {
                    sb.append(TAB);
                }
                sb.append(val).append('\n');
            }

            super.write(sb.toString());

        } // END writeText


        public void startElement(String name,
                          Map<String,String> attributes) throws IOException {

            if (name == null) {
                throw new IllegalArgumentException("Null element name");
            }

            StringBuffer sb = new StringBuffer();
            for (int i = 0, size = elementStack.size(); i < size; i++) {
                sb.append(TAB);
            }

            elementStack.push(name);

            String attributesString = null;
            if (attributes != null) {
                attributesString = createAttributesString(attributes);
            }


            sb.append('<').append(name);
            if (attributesString != null) {
                sb.append(attributesString);
            }
            sb.append(">\n");


            super.write(sb.toString());

        } // END startElement


        public void startElement(String name) throws IOException {

            startElement(name, null);

        } // END startElement


        public void closeElement(int elementCount) throws IOException {

            if (elementStack.size() == 0) {
                throw new IllegalStateException("Cannot close element - " +
                    "no elements on stack");
            }

            StringBuffer sb = new StringBuffer();
            for (int count = 0; count < elementCount; count++) {
                String elementName = elementStack.pop();

                for (int i = 0, size = elementStack.size(); i < size; i++) {
                    sb.append(TAB);
                }

                sb.append("</").append(elementName).append(">\n");
            }

            super.write(sb.toString());

        } // END closeElement


        public void closeElement() throws IOException {

            closeElement(1);

        } // END closeElement


        public void closeAllElements() throws IOException {

            closeElement(elementStack.size());

        } // END closeAllElements()


        public void writeComment(String value) throws IOException {

            StringBuffer sb =
                new StringBuffer(value.length() + NUM_COMMENT_CHARS);
            sb.append(START_COMMENT).append(value).append(END_COMMENT);

            super.write(sb.toString());

        } // END writeComment


        // ----------------------------------------------------- Private Methods


        private String createAttributesString(Map<String,String> attributes) {

            if (attributes == null) {
                throw new IllegalArgumentException("Null attributes map");
            }

            StringBuffer sb = new StringBuffer();
	    for (Map.Entry entry : attributes.entrySet()) {
                String name = (String)entry.getKey();
                sb.append(' ').append(name).append('=');
                sb.append('"').append((String)entry.getValue()).append('"');
            }

            return sb.toString();

        } // END createAttributesString

    } // END XMLWriter
}
