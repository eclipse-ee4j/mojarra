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

package com.sun.faces.config.manager.tasks;

import static com.sun.faces.RIConstants.CHAR_ENCODING;
import static com.sun.faces.RIConstants.DOCUMENT_NAMESPACE;
import static com.sun.faces.config.manager.DbfFactory.FACES_ENTITY_RESOLVER;
import static com.sun.faces.config.manager.DbfFactory.FACES_ERROR_HANDLER;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_CONFIG_1_X_DEFAULT_NS;
import static com.sun.faces.config.manager.FacesSchema.Schemas.JAKARTAEE_SCHEMA_DEFAULT_NS;
import static com.sun.faces.config.manager.FacesSchema.Schemas.JAVAEE_SCHEMA_DEFAULT_NS;
import static com.sun.faces.config.manager.FacesSchema.Schemas.JAVAEE_SCHEMA_LEGACY_DEFAULT_NS;
import static com.sun.faces.config.processor.FacesFlowDefinitionConfigProcessor.synthesizeEmptyFlowDefinition;
import static com.sun.faces.config.processor.FacesFlowDefinitionConfigProcessor.uriIsFlowDefinition;
import static com.sun.faces.util.Util.createTransformerFactory;
import static java.lang.System.arraycopy;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.WARNING;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.faces.config.ConfigManager;
import com.sun.faces.config.ConfigurationException;
import com.sun.faces.config.manager.DbfFactory;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Timer;

import jakarta.servlet.ServletContext;

/**
 * This <code>Callable</code> will be used by <code>getXMLDocuments</code>
 * It represents a single configuration resource (such as faces-config.xml) to be parsed into a DOM.
 */
public class ParseConfigResourceToDOMTask implements Callable<DocumentInfo> {

    /**
     * Name of the attribute added by ParseTask to indicate a {@link Document} instance as a representation of
     * <code>/WEB-INF/faces-config.xml</code>.
     */
    public static final String WEB_INF_MARKER = "com.sun.faces.webinf";

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    private static final String EMPTY_FACES_CONFIG = "com/sun/faces/empty-faces-config.xml";
    private static final String FACES_CONFIG_TAGNAME = "faces-config";
    private static final String FACELET_TAGLIB_TAGNAME = "facelet-taglib";

    /**
     * Stylesheet to convert 1.0 and 1.1 based faces-config documents to our private 1.1 schema for validation.
     */
    private static final String FACES_TO_1_1_PRIVATE_XSL = "/com/sun/faces/faces1_0-1_1toSchema.xsl";

    private final ServletContext servletContext;
    private final URI documentURI;
    private DocumentBuilderFactory factory;
    private final boolean validating;



    // --------------------------------------------------------
    // Constructors

    /**
     * <p>
     * Constructs a new ParseTask instance
     * </p>
     *
     * @param servletContext the servlet context.
     * @param validating whether or not we're validating
     * @param documentURI a URL to the configuration resource to be parsed
     * @throws Exception general error
     */
    public ParseConfigResourceToDOMTask(ServletContext servletContext, boolean validating, URI documentURI) throws Exception {
        this.servletContext = servletContext;
        this.documentURI = documentURI;
        this.validating = validating;
    }



    // ----------------------------------------------- Methods from Callable

    /**
     * @return the result of the parse operation (a DOM)
     * @throws Exception if an error occurs during the parsing process
     */
    @Override
    public DocumentInfo call() throws Exception {
        try {
            Timer timer = Timer.getInstance();
            if (timer != null) {
                timer.startTiming();
            }

            Document document = getDocument();

            if (timer != null) {
                timer.stopTiming();
                timer.logResult("Parse " + documentURI.toURL().toExternalForm());
            }

            return new DocumentInfo(document, documentURI);
        } catch (Exception e) {
            throw new ConfigurationException(format("Unable to parse document ''{0}'': {1}", documentURI.toURL().toExternalForm(), e.getMessage()), e);
        }
    }



    // ----------------------------------------------------- Private Methods

    /**
     * @return <code>Document</code> based on <code>documentURI</code>.
     * @throws Exception if an error occurs during the process of building a <code>Document</code>
     */
    private Document getDocument() throws Exception {
        DocumentBuilder documentBuilder = getNonValidatingBuilder();
        URL documentURL = documentURI.toURL();

        Document configDocument = parseDocumentFromURL(documentBuilder, documentURL);

        String documentNS = null;
        if (configDocument == null) {
            if (uriIsFlowDefinition(documentURI)) {
                documentNS = DOCUMENT_NAMESPACE;
                configDocument = synthesizeEmptyFlowDefinition(documentURI);
            }
        } else {
            Element documentElement = configDocument.getDocumentElement();
            documentNS = documentElement.getNamespaceURI();
            String rootElementTagName = documentElement.getTagName();

            if (isNonFacesConfigDocument(rootElementTagName)) {

                if (LOGGER.isLoggable(WARNING)) {
                    LOGGER.log(WARNING,
                            MessageFormat.format("Config document {0} with namespace URI {1} is not a faces-config or facelet-taglib file.  Ignoring.",
                                    documentURI.toURL().toExternalForm(), documentNS));
                }
                // Ignore by returning an empty document instead of null
                return getEmptyFacesConfig(documentBuilder);
            }
        }

        Document returnDoc = configDocument;

        if (validating && documentNS != null) {
            DOMSource domSource = new DOMSource(configDocument, documentURL.toExternalForm());

            /*
             * If the Document in question is 1.2 (i.e. it has a namespace matching JAVAEE_SCHEMA_DEFAULT_NS, then perform
             * validation using the cached schema and return. Otherwise we assume a 1.0 or 1.1 faces-config in which case we need to
             * transform it to reference a special 1.1 schema before validating.
             */
            Node documentElement = ((Document) domSource.getNode()).getDocumentElement();

            switch (documentNS) {
            case JAKARTAEE_SCHEMA_DEFAULT_NS:
            case JAVAEE_SCHEMA_DEFAULT_NS:
            case JAVAEE_SCHEMA_LEGACY_DEFAULT_NS: {

                // If the Document in question is 1.2+ (i.e. it has a namespace matching JAVAEE_SCHEMA_LEGACY_DEFAULT_NS or later,
                // then perform validation using the cached schema and return.

                returnDoc = loadDocument(
                    findMatchingSchema(documentNS, getVersion(documentElement), documentElement.getLocalName()),
                    domSource);
                break;
            }

            default:
                // Assume a 1.0 or 1.1 faces-config in which case we need to transform it to reference a special 1.1 schema
                // before validating.

                returnDoc = loadDocument(
                    findMatchingSchema(documentNS, null, null),
                    transformDocument(documentNS, domSource));
            }
        }

        // Mark this document as the parsed representation of the WEB-INF/faces-config.xml.
        // This is used later in the configuration processing.
        if (documentURL.toExternalForm().contains("/WEB-INF/faces-config.xml")) {
            Attr webInf = returnDoc.createAttribute(WEB_INF_MARKER);
            webInf.setValue("true");
            returnDoc.getDocumentElement().getAttributes().setNamedItem(webInf);
        }

        return returnDoc;
    }

    private Document parseDocumentFromURL(DocumentBuilder documentBuilder, URL documentURL) throws SAXException, IOException {
        InputSource documentInputSource = new InputSource(getInputStream(documentURL));
        documentInputSource.setSystemId(documentURI.toURL().toExternalForm());

        try {
            return documentBuilder.parse(documentInputSource);
        } catch (SAXParseException spe) {
            // [mojarra-1693]
            // Test if this is a zero length or whitespace only faces-config.xml file.
            // If so, just make an empty Document
            InputStream stream = documentInputSource.getByteStream();
            stream.close();

            stream = new InputSource(getInputStream(documentURL)).getByteStream();
            if (streamIsZeroLengthOrEmpty(stream) && documentURL.toExternalForm().endsWith("faces-config.xml")) {
                return documentBuilder.parse(new InputSource(getInputStream(getClass().getClassLoader().getResource(EMPTY_FACES_CONFIG))));
            }
        }

        return null;
    }

    private Schema findMatchingSchema(String documentNS, String version, String localName) {
        return DbfFactory.getSchema(servletContext, documentNS, version, localName);
    }

    private Document loadDocument(Schema schema, DOMSource domSource) throws Exception {
        DocumentBuilder builder = getBuilderForSchema(schema);
        if (builder.isValidating()) {
            builder.getSchema().newValidator().validate(domSource);
        }

        return (Document) domSource.getNode();
    }

    private Document loadDocument(Schema schema, DOMResult domResult) throws Exception {
        DocumentBuilder builder = getBuilderForSchema(schema);
        if (builder.isValidating()) {
            builder.getSchema().newValidator().validate(new DOMSource(domResult.getNode()));
        }

        return (Document) domResult.getNode();
    }

    private String getVersion(Node documentElement) {
        Attr version = (Attr) documentElement.getAttributes().getNamedItem("version");
        if (version == null) {
            throw new ConfigurationException("No document version available.");
        }

        return version.getValue();
    }

    private boolean streamIsZeroLengthOrEmpty(InputStream is) throws IOException {
        boolean isZeroLengthOrEmpty = 0 == is.available();
        final int size = 1024;
        byte[] buffer = new byte[size];

        while (!isZeroLengthOrEmpty && -1 != is.read(buffer, 0, size)) {
            String bufferAsString = new String(buffer, CHAR_ENCODING).trim();
            isZeroLengthOrEmpty = 0 == bufferAsString.length();
            buffer[0] = 0;
            for (int i = 1; i < size; i += i) {
                arraycopy(buffer, 0, buffer, i, size - i < i ? size - i : i);
            }
        }

        return isZeroLengthOrEmpty;
    }

    private static DOMResult transformDocument(String documentNS, DOMSource domSource) throws Exception {
        DOMResult domResult = new DOMResult();
        getTransformer(documentNS).transform(domSource, domResult);

        // Copy the source document URI to the transformed result so that processes that need to
        // build URLs relative to the document will work as expected.
        ((Document) domResult.getNode()).setDocumentURI(((Document) domSource.getNode()).getDocumentURI());

        return domResult;
    }

    /**
     * Obtain a <code>Transformer</code> using the style sheet referenced by the <code>XSL</code> constant.
     *
     * @return a new Tranformer instance
     * @throws Exception if a Tranformer instance could not be created
     */
    private static Transformer getTransformer(String documentNS) throws Exception {
        String xslToApply;
        switch (documentNS) {
            case FACES_CONFIG_1_X_DEFAULT_NS:
                xslToApply = FACES_TO_1_1_PRIVATE_XSL;
                break;
            default:
                throw new IllegalStateException();
            }

        return createTransformerFactory().newTransformer(new StreamSource(getInputStream(ConfigManager.class.getResource(xslToApply))));
    }

    /**
     * @return an <code>InputStream</code> to the resource referred to by <code>url</code>
     * @param url source <code>URL</code>
     * @throws IOException if an error occurs
     */
    private static InputStream getInputStream(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);

        return new BufferedInputStream(connection.getInputStream());
    }

    private DocumentBuilder getNonValidatingBuilder() throws Exception {
        DocumentBuilderFactory tFactory = DbfFactory.getFactory();
        tFactory.setValidating(false);

        DocumentBuilder tBuilder = tFactory.newDocumentBuilder();
        tBuilder.setEntityResolver(FACES_ENTITY_RESOLVER);
        tBuilder.setErrorHandler(FACES_ERROR_HANDLER);

        return tBuilder;
    }

    private DocumentBuilder getBuilderForSchema(Schema schema) throws Exception {
        factory = DbfFactory.getFactory();

        try {
            factory.setSchema(schema);
        } catch (UnsupportedOperationException upe) {
            return getNonValidatingBuilder();
        }

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(FACES_ENTITY_RESOLVER);
        builder.setErrorHandler(FACES_ERROR_HANDLER);

        return builder;
    }

    private boolean isNonFacesConfigDocument(String rootElementTagName) {
        return !FACES_CONFIG_TAGNAME.equals(rootElementTagName) && !FACELET_TAGLIB_TAGNAME.equals(rootElementTagName);
    }

    private Document getEmptyFacesConfig(DocumentBuilder documentBuilder) throws SAXException, IOException {
        return documentBuilder.parse(
                new InputSource(getInputStream(getClass().getClassLoader().getResource(EMPTY_FACES_CONFIG))));
    }

}
