/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.compiler;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.DisallowDoctypeDecl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.faces.RIConstants;
import com.sun.faces.config.FaceletsConfiguration;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.facelets.tag.TagAttributeImpl;
import com.sun.faces.facelets.tag.TagAttributesImpl;
import com.sun.faces.facelets.tag.faces.core.CoreLibrary;
import com.sun.faces.util.Util;

import jakarta.faces.component.html.HtmlDoctype;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttributes;

/**
 * Compiler implementation that uses SAX
 *
 * @author Jacob Hookom
 * @see Compiler
 * @version $Id$
 */
public final class SAXCompiler extends Compiler {

    private final static Pattern XmlDeclaration = Pattern.compile("^<\\?xml.+?version=['\"](.+?)['\"](.+?encoding=['\"]((.+?))['\"])?.*?\\?>");

    private static class CompilationHandler extends DefaultHandler implements LexicalHandler {

        protected final String alias;

        protected boolean inDocument = false;

        protected Locator locator;

        protected final CompilationManager unit;

        public CompilationHandler(CompilationManager unit, String alias) {
            this.unit = unit;
            this.alias = alias;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inDocument) {
                unit.writeText(new String(ch, start, length));
            }
        }

        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {
            if (inDocument) {
                if (!unit.getWebConfiguration().getFaceletsConfiguration().isConsumeComments(alias)) {
                    unit.writeComment(new String(ch, start, length));
                }
            }
        }

        protected TagAttributesImpl createAttributes(Attributes attrs) {
            int len = attrs.getLength();
            TagAttributeImpl[] ta = new TagAttributeImpl[len];
            for (int i = 0; i < len; i++) {
                ta[i] = new TagAttributeImpl(createLocation(), attrs.getURI(i), attrs.getLocalName(i), attrs.getQName(i), attrs.getValue(i));
            }
            return new TagAttributesImpl(ta);
        }

        protected Location createLocation() {
            Location result = null;
            if (null != locator) {
                result = new Location(alias, locator.getLineNumber(), locator.getColumnNumber());
            } else {
                if (log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, "Unable to create Location due to null locator instance variable.");
                }
            }
            return result;
        }

        @Override
        public void endCDATA() throws SAXException {
            if (inDocument) {
                if (!unit.getWebConfiguration().getFaceletsConfiguration().isConsumeCDATA(alias)) {
                    unit.writeInstruction("]]>");
                }
            }
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void endDTD() throws SAXException {
            inDocument = true;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            unit.popTag();
        }

        @Override
        public void endEntity(String name) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            unit.popNamespace(prefix);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            if (locator != null) {
                throw new SAXException("Error Traced[line: " + locator.getLineNumber() + "] " + e.getMessage());
            } else {
                throw e;
            }
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            if (inDocument) {
                unit.writeWhitespace(new String(ch, start, length));
            }
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
            String dtd = "com/sun/faces/xhtml/default.dtd";
            /*
             * if ("-//W3C//DTD XHTML 1.0 Transitional//EN".equals(publicId)) { dtd = "xhtml1-transitional.dtd"; } else if (systemId
             * != null && systemId.startsWith("file:/")) { return new InputSource(systemId); }
             */
            URL url = this.getClass().getClassLoader().getResource(dtd);
            return new InputSource(url.toString());
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startCDATA() throws SAXException {
            if (inDocument) {
                if (!unit.getWebConfiguration().getFaceletsConfiguration().isConsumeCDATA(alias)) {
                    unit.writeInstruction("<![CDATA[");
                }
            }
        }

        @Override
        public void startDocument() throws SAXException {
            inDocument = true;
        }

        @Override
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
            // If there is a process-as value for the extension, only allow
            // the PI to be written if its value is xhtml
            FaceletsConfiguration facelets = unit.getWebConfiguration().getFaceletsConfiguration();
            boolean processAsXhtml = facelets.isProcessCurrentDocumentAsFaceletsXhtml(alias);
            boolean outputAsHtml5 = facelets.isOutputHtml5Doctype(alias);

            if (inDocument && (processAsXhtml || outputAsHtml5)) {
                HtmlDoctype doctype = new HtmlDoctype();
                doctype.setRootElement(name);

                if (!outputAsHtml5) {
                    doctype.setPublic(publicId);
                    doctype.setSystem(systemId);
                }

                // It is essential to save the doctype here because this is the
                // *only* time we will have access to it.
                Util.saveDOCTYPEToFacesContextAttributes(doctype);
            }
            inDocument = false;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            TagAttributes tagAttrs = createAttributes(attributes);
            Tag tag = new Tag(createLocation(), uri, localName, qName, tagAttrs);
            tagAttrs.setTag(tag);
            unit.pushTag(tag);

        }

        @Override
        public void startEntity(String name) throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            unit.pushNamespace(prefix, uri);
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            if (inDocument) {

                // If there is a process-as value for the extension, only allow
                // the PI to be written if its value is xhtml
                boolean processAsXhtml = unit.getWebConfiguration().getFaceletsConfiguration().isProcessCurrentDocumentAsFaceletsXhtml(alias);

                if (processAsXhtml) {
                    unit.writeInstruction("<?" + target + ' ' + data + "?>\n");
                }
            }
        }

        protected boolean isDisallowDoctypeDeclSet() {
            return unit.getWebConfiguration().isSet(DisallowDoctypeDecl);
        }

        protected boolean isDisallowDoctypeDecl() {
            return unit.getWebConfiguration().isOptionEnabled(DisallowDoctypeDecl);
        }
    }

    private static class MetadataCompilationHandler extends CompilationHandler {

        private static final String METADATA_HANDLER = "metadata";
        private boolean processingMetadata = false;
        private boolean metadataProcessed = false;

        // -------------------------------------------------------- Constructors

        public MetadataCompilationHandler(CompilationManager unit, String alias) {

            super(unit, alias);

        }

        // ------------------------------------- Methods from CompilationHandler

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (!metadataProcessed) {
                if (processingMetadata) {
                    // PENDING consider optimizing this to be a no-op
                    // on whitespace, but don't instantiate the String
                    // just to test that.
                    unit.writeText(new String(ch, start, length));
                }
            }

        }

        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {
            // no-op
        }

        @Override
        public void endCDATA() throws SAXException {
            // no-op
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            // no-op
        }

        @Override
        public void startCDATA() throws SAXException {
            // no-op
        }

        @Override
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
            // no-op
        }

        @Override
        public void startEntity(String name) throws SAXException {
            // no-op
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            // no-op
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (!metadataProcessed) {
                if (!processingMetadata && CoreLibrary.NAMESPACES.contains(uri)) {
                    if (METADATA_HANDLER.equals(localName)) {
                        processingMetadata = true;
                    }
                }
                if (processingMetadata) {
                    super.startElement(uri, localName, qName, attributes);
                }
            }
            if (localName.equals("view") && CoreLibrary.NAMESPACES.contains(uri)) {
                super.startElement(uri, localName, qName, attributes);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (!metadataProcessed) {
                if (processingMetadata) {
                    super.endElement(uri, localName, qName);
                }
                if (processingMetadata && CoreLibrary.NAMESPACES.contains(uri)) {
                    if (METADATA_HANDLER.equals(localName)) {
                        processingMetadata = false;
                        metadataProcessed = true;
                    }
                }
            }
            if (localName.equals("view") && CoreLibrary.NAMESPACES.contains(uri)) {
                super.endElement(uri, localName, qName);
            }
        }

    }

    public SAXCompiler() {
        super();
    }

    @Override
    public FaceletHandler doCompile(URL src, String alias) throws IOException {

        CompilationManager mgr = new CompilationManager(alias, this);
        CompilationHandler handler = new CompilationHandler(mgr, alias);
        return doCompile(mgr, handler, src, alias);

    }

    @Override
    public FaceletHandler doMetadataCompile(URL src, String alias) throws IOException {

        CompilationManager mgr = new CompilationManager("metadata/" + alias, this);
        CompilationHandler handler = new MetadataCompilationHandler(mgr, alias);
        return doCompile(mgr, handler, src, alias);
    }

    protected FaceletHandler doCompile(CompilationManager mngr, CompilationHandler handler, URL src, String alias) throws IOException {

        String encoding = getEncoding();
        try (InputStream is = new BufferedInputStream(src.openStream(), 1024);) {

            writeXmlDecl(is, encoding, mngr);
            SAXParser parser = createSAXParser(handler);
            parser.parse(is, handler);
        } catch (SAXException e) {
            throw new FaceletException("Error Parsing " + alias + ": " + e.getMessage(), e.getCause());
        } catch (ParserConfigurationException e) {
            throw new FaceletException("Error Configuring Parser " + alias + ": " + e.getMessage(), e.getCause());
        } catch (FaceletException e) {
            throw e;
        }
        FaceletHandler result = new EncodingHandler(mngr.createFaceletHandler(), encoding, mngr.getCompilationMessageHolder());
        mngr.setCompilationMessageHolder(null);

        return result;

    }

    private String getEncoding() {
        String result;
        String encodingFromRequest = null;
        FacesContext context = FacesContext.getCurrentInstance();
        if (null != context) {
            ExternalContext extContext = context.getExternalContext();
            encodingFromRequest = extContext.getRequestCharacterEncoding();
        }
        result = null != encodingFromRequest ? encodingFromRequest : RIConstants.CHAR_ENCODING;

        return result;
    }

    protected static void writeXmlDecl(InputStream is, String encoding, CompilationManager mngr) throws IOException {
        is.mark(128);
        try {
            byte[] b = new byte[128];
            if (is.read(b) > 0) {
                String r = new String(b, encoding);
                Matcher m = XmlDeclaration.matcher(r);
                if (m.find()) {
                    WebConfiguration config = mngr.getWebConfiguration();
                    FaceletsConfiguration faceletsConfig = config.getFaceletsConfiguration();
                    boolean currentModeIsXhtml = faceletsConfig.isProcessCurrentDocumentAsFaceletsXhtml(mngr.getAlias());

                    // We want to write the XML declaration if and only if
                    // the file extension for the current file has a mapping
                    // with the value of XHTML
                    if (currentModeIsXhtml) {
                        Util.saveXMLDECLToFacesContextAttributes(m.group(0) + "\n");
                    }
                }
            }
        } finally {
            is.reset();
        }
    }

    private SAXParser createSAXParser(CompilationHandler handler) throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = Util.createSAXParserFactory();
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        factory.setFeature("http://xml.org/sax/features/validation", isValidating());
        factory.setValidating(isValidating());
        if (handler.isDisallowDoctypeDeclSet()) {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", handler.isDisallowDoctypeDecl());
        }
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        reader.setErrorHandler(handler);
        reader.setEntityResolver(handler);
        return parser;
    }

}
