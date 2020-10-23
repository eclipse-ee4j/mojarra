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

import static com.sun.faces.config.manager.DbfFactory.FACES_ENTITY_RESOLVER;
import static com.sun.faces.config.manager.DbfFactory.FACES_ERROR_HANDLER;
import static com.sun.faces.util.Util.createTransformerFactory;
import static java.text.MessageFormat.format;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.sun.faces.RIConstants;
import com.sun.faces.config.ConfigManager;
import com.sun.faces.config.ConfigurationException;
import com.sun.faces.config.manager.DbfFactory;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.config.processor.FacesFlowDefinitionConfigProcessor;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Timer;

import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>Callable</code> will be used by
 * {@link ConfigManager#getXMLDocuments(jakarta.servlet.ServletContext, java.util.List, java.util.concurrent.ExecutorService, boolean)}.
 * It represents a single configuration resource to be parsed into a DOM.
 * </p>
 */
public class ParseConfigResourceToDOMTask implements Callable<DocumentInfo> {

    /**
     * Name of the attribute added by ParseTask to indicate a {@link Document} instance as a representation of
     * <code>/WEB-INF/faces-config.xml</code>.
     */
    public static final String WEB_INF_MARKER = "com.sun.faces.webinf";

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    private static final String JAVAEE_SCHEMA_LEGACY_DEFAULT_NS = "http://java.sun.com/xml/ns/javaee";
    private static final String JAVAEE_SCHEMA_DEFAULT_NS = "http://xmlns.jcp.org/xml/ns/javaee";
    private static final String JAKARTAEE_SCHEMA_DEFAULT_NS = "https://jakarta.ee/xml/ns/jakartaee";
    private static final String EMPTY_FACES_CONFIG = "com/sun/faces/empty-faces-config.xml";
    private static final String FACES_CONFIG_TAGNAME = "faces-config";
    private static final String FACELET_TAGLIB_TAGNAME = "facelet-taglib";

    private static final String FACES_CONFIG_1_X_DEFAULT_NS = "http://java.sun.com/JSF/Configuration";

    private static final String FACELETS_1_0_DEFAULT_NS = "http://java.sun.com/JSF/Facelet";

    /**
     * Stylesheet to convert 1.0 and 1.1 based faces-config documents to our private 1.1 schema for validation.
     */
    private static final String FACES_TO_1_1_PRIVATE_XSL = "/com/sun/faces/jsf1_0-1_1toSchema.xsl";

    /**
     * Stylesheet to convert 1.0 facelet-taglib documents from 1.0 to 2.0 for schema validation purposes.
     */
    private static final String FACELETS_TO_2_0_XSL = "/com/sun/faces/facelets1_0-2_0toSchema.xsl";

    private ServletContext servletContext;
    private URI documentURI;
    private DocumentBuilderFactory factory;
    private boolean validating;


    // --------------------------------------------------------
    // Constructors

    /**
     * <p>
     * Constructs a new ParseTask instance
     * </p>
     *
     * @param servletContext
     *            the servlet context.
     * @param validating
     *            whether or not we're validating
     * @param documentURI
     *            a URL to the configuration resource to be parsed
     * @throws Exception
     *             general error
     */
    public ParseConfigResourceToDOMTask(ServletContext servletContext, boolean validating, URI documentURI) throws Exception {
        this.servletContext = servletContext;
        this.documentURI = documentURI;
        this.validating = validating;
    }

    // ----------------------------------------------- Methods from
    // Callable

    /**
     * @return the result of the parse operation (a DOM)
     * @throws Exception
     *             if an error occurs during the parsing process
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
            throw new ConfigurationException(
                    format("Unable to parse document ''{0}'': {1}", documentURI.toURL().toExternalForm(), e.getMessage()), e);
        }
    }


    // ----------------------------------------------------- Private
    // Methods

    /**
     * @return <code>Document</code> based on <code>documentURI</code>.
     * @throws Exception
     *             if an error occurs during the process of building a <code>Document</code>
     */
    private Document getDocument() throws Exception {

        Document returnDoc;
        DocumentBuilder db = getNonValidatingBuilder();
        URL documentURL = documentURI.toURL();
        InputSource is = new InputSource(getInputStream(documentURL));
        is.setSystemId(documentURI.toURL().toExternalForm());
        Document doc = null;

        try {
            doc = db.parse(is);
        } catch (SAXParseException spe) {
            // [mojarra-1693]
            // Test if this is a zero length or whitespace only
            // faces-config.xml file.
            // If so, just make an empty Document
            InputStream stream = is.getByteStream();
            stream.close();

            is = new InputSource(getInputStream(documentURL));
            stream = is.getByteStream();
            if (streamIsZeroLengthOrEmpty(stream) && documentURL.toExternalForm().endsWith("faces-config.xml")) {
                ClassLoader loader = this.getClass().getClassLoader();
                is = new InputSource(getInputStream(loader.getResource(EMPTY_FACES_CONFIG)));
                doc = db.parse(is);
            }

        }

        String documentNS = null;
        if (null == doc) {
            if (FacesFlowDefinitionConfigProcessor.uriIsFlowDefinition(documentURI)) {
                documentNS = RIConstants.JAVAEE_XMLNS;
                doc = FacesFlowDefinitionConfigProcessor.synthesizeEmptyFlowDefinition(documentURI);
            }
        } else {
            Element documentElement = doc.getDocumentElement();
            documentNS = documentElement.getNamespaceURI();
            String rootElementTagName = documentElement.getTagName();

            boolean isNonFacesConfigDocument = !FACES_CONFIG_TAGNAME.equals(rootElementTagName)
                    && !FACELET_TAGLIB_TAGNAME.equals(rootElementTagName);

            if (isNonFacesConfigDocument) {
                ClassLoader loader = this.getClass().getClassLoader();
                is = new InputSource(getInputStream(loader.getResource(EMPTY_FACES_CONFIG)));
                doc = db.parse(is);
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING,
                            MessageFormat.format(
                                    "Config document {0} with namespace URI {1} is not a faces-config or facelet-taglib file.  Ignoring.",
                                    documentURI.toURL().toExternalForm(), documentNS));
                }
                return doc;
            }
        }

        if (validating && documentNS != null) {
            DOMSource domSource = new DOMSource(doc, documentURL.toExternalForm());

            /*
             * If the Document in question is 1.2 (i.e. it has a namespace matching JAVAEE_SCHEMA_DEFAULT_NS, then perform
             * validation using the cached schema and return. Otherwise we assume a 1.0 or 1.1 faces-config in which case we need to
             * transform it to reference a special 1.1 schema before validating.
             */
            Node documentElement = ((Document) domSource.getNode()).getDocumentElement();
            switch (documentNS) {
            case JAVAEE_SCHEMA_DEFAULT_NS: {
                Attr version = (Attr) documentElement.getAttributes().getNamedItem("version");
                Schema schema;
                if (version != null) {
                    String versionStr = version.getValue();
                    switch (versionStr) {
                    case "2.2":
                        if ("facelet-taglib".equals(documentElement.getLocalName())) {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACELET_TAGLIB_22);
                        } else {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACES_22);
                        }
                        break;
                    case "2.3":
                        if ("facelet-taglib".equals(documentElement.getLocalName())) {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACELET_TAGLIB_22);
                        } else {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACES_23);
                        }
                        break;
                    default:
                        throw new ConfigurationException("Unknown Schema version: " + versionStr);
                    }
                    DocumentBuilder builder = getBuilderForSchema(schema);
                    if (builder.isValidating()) {
                        builder.getSchema().newValidator().validate(domSource);
                        returnDoc = ((Document) domSource.getNode());
                    } else {
                        returnDoc = ((Document) domSource.getNode());
                    }
                } else {
                    // this shouldn't happen, but...
                    throw new ConfigurationException("No document version available.");
                }
                break;
            }
            case JAKARTAEE_SCHEMA_DEFAULT_NS: {
                Attr version = (Attr) documentElement.getAttributes().getNamedItem("version");
                Schema schema;
                if (version != null) {
                    String versionStr = version.getValue();
                    switch (versionStr) {
                    case "3.0":
                        if ("facelet-taglib".equals(documentElement.getLocalName())) {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACELET_TAGLIB_22);
                        } else {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACES_30);
                        }
                        break;
                    default:
                        throw new ConfigurationException("Unknown Schema version: " + versionStr);
                    }
                    DocumentBuilder builder = getBuilderForSchema(schema);
                    if (builder.isValidating()) {
                        builder.getSchema().newValidator().validate(domSource);
                        returnDoc = ((Document) domSource.getNode());
                    } else {
                        returnDoc = ((Document) domSource.getNode());
                    }
                } else {
                    // this shouldn't happen, but...
                    throw new ConfigurationException("No document version available.");
                }
                break;
            }
            case JAVAEE_SCHEMA_LEGACY_DEFAULT_NS: {
                Attr version = (Attr) documentElement.getAttributes().getNamedItem("version");
                Schema schema;
                if (version != null) {
                    String versionStr = version.getValue();
                    switch (versionStr) {
                    case "2.0":
                        if ("facelet-taglib".equals(documentElement.getLocalName())) {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACELET_TAGLIB_20);
                        } else {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACES_20);
                        }
                        break;
                    case "2.1":
                        if ("facelet-taglib".equals(documentElement.getLocalName())) {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACELET_TAGLIB_20);
                        } else {
                            schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACES_21);
                        }
                        break;
                    case "1.2":
                        schema = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACES_12);
                        break;
                    default:
                        throw new ConfigurationException("Unknown Schema version: " + versionStr);
                    }
                    DocumentBuilder builder = getBuilderForSchema(schema);
                    if (builder.isValidating()) {
                        builder.getSchema().newValidator().validate(domSource);
                        returnDoc = ((Document) domSource.getNode());
                    } else {
                        returnDoc = ((Document) domSource.getNode());
                    }
                } else {
                    // this shouldn't happen, but...
                    throw new ConfigurationException("No document version available.");
                }
                break;
            }
            default:
                DOMResult domResult = new DOMResult();
                Transformer transformer = getTransformer(documentNS);
                transformer.transform(domSource, domResult);

                // copy the source document URI to the transformed
                // result
                // so that processes that need to build URLs relative
                // to the
                // document will work as expected.
                ((Document) domResult.getNode()).setDocumentURI(((Document) domSource.getNode()).getDocumentURI());
                Schema schemaToApply;

                switch (documentNS) {
                case FACES_CONFIG_1_X_DEFAULT_NS:
                    schemaToApply = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACES_11);
                    break;
                case FACELETS_1_0_DEFAULT_NS:
                    schemaToApply = DbfFactory.getSchema(servletContext, DbfFactory.FacesSchema.FACELET_TAGLIB_20);
                    break;
                default:
                    throw new IllegalStateException();
                }

                DocumentBuilder builder = getBuilderForSchema(schemaToApply);
                if (builder.isValidating()) {
                    builder.getSchema().newValidator().validate(new DOMSource(domResult.getNode()));
                    returnDoc = (Document) domResult.getNode();
                } else {
                    returnDoc = (Document) domResult.getNode();
                }
            }
        } else {
            returnDoc = doc;
        }

        // mark this document as the parsed representation of the
        // WEB-INF/faces-config.xml. This is used later in the
        // configuration
        // processing.
        if (documentURL.toExternalForm().contains("/WEB-INF/faces-config.xml")) {
            Attr webInf = returnDoc.createAttribute(WEB_INF_MARKER);
            webInf.setValue("true");
            returnDoc.getDocumentElement().getAttributes().setNamedItem(webInf);
        }

        return returnDoc;

    }

    private boolean streamIsZeroLengthOrEmpty(InputStream is) throws IOException {
        boolean isZeroLengthOrEmpty = (0 == is.available());
        final int size = 1024;
        byte[] b = new byte[size];
        String s;
        while (!isZeroLengthOrEmpty && -1 != is.read(b, 0, size)) {
            s = (new String(b, RIConstants.CHAR_ENCODING)).trim();
            isZeroLengthOrEmpty = 0 == s.length();
            b[0] = 0;
            for (int i = 1; i < size; i += i) {
                System.arraycopy(b, 0, b, i, ((size - i) < i) ? (size - i) : i);
            }
        }

        return isZeroLengthOrEmpty;
    }

    /**
     * Obtain a <code>Transformer</code> using the style sheet referenced by the <code>XSL</code> constant.
     *
     * @return a new Tranformer instance
     * @throws Exception
     *             if a Tranformer instance could not be created
     */
    private static Transformer getTransformer(String documentNS) throws Exception {

        TransformerFactory factory = createTransformerFactory();

        String xslToApply;
        switch (documentNS) {
        case FACES_CONFIG_1_X_DEFAULT_NS:
            xslToApply = FACES_TO_1_1_PRIVATE_XSL;
            break;
        case FACELETS_1_0_DEFAULT_NS:
            xslToApply = FACELETS_TO_2_0_XSL;
            break;
        default:
            throw new IllegalStateException();
        }

        return factory.newTransformer(new StreamSource(getInputStream(ConfigManager.class.getResource(xslToApply))));
    }

    /**
     * @return an <code>InputStream</code> to the resource referred to by <code>url</code>
     * @param url
     *            source <code>URL</code>
     * @throws IOException
     *             if an error occurs
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
        this.factory = DbfFactory.getFactory();

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

} // END ParseTask
