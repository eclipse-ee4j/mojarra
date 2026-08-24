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

package org.glassfish.mojarra.config.manager.tasks;

import static java.lang.System.arraycopy;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.WARNING;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static org.glassfish.mojarra.RIConstants.CHAR_ENCODING;
import static org.glassfish.mojarra.config.manager.DbfFactory.FACES_ENTITY_RESOLVER;
import static org.glassfish.mojarra.config.manager.DbfFactory.FACES_ERROR_HANDLER;
import static org.glassfish.mojarra.config.manager.FacesSchema.CURRENT_NAMESPACE;
import static org.glassfish.mojarra.config.manager.FacesSchema.Schemas.FACES_CONFIG_1_X_DEFAULT_NS;
import static org.glassfish.mojarra.config.manager.FacesSchema.Schemas.JAKARTAEE_SCHEMA_DEFAULT_NS;
import static org.glassfish.mojarra.config.manager.FacesSchema.Schemas.JAVAEE_SCHEMA_DEFAULT_NS;
import static org.glassfish.mojarra.config.manager.FacesSchema.Schemas.JAVAEE_SCHEMA_LEGACY_DEFAULT_NS;
import static org.glassfish.mojarra.config.processor.FacesFlowDefinitionConfigProcessor.synthesizeEmptyFlowDefinition;
import static org.glassfish.mojarra.config.processor.FacesFlowDefinitionConfigProcessor.uriIsFlowDefinition;
import static org.glassfish.mojarra.util.Util.createTransformerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.config.ConfigManager;
import org.glassfish.mojarra.config.ConfigurationException;
import org.glassfish.mojarra.config.manager.DbfFactory;
import org.glassfish.mojarra.config.manager.documents.DocumentInfo;
import org.glassfish.mojarra.util.FacesLogger;
import org.glassfish.mojarra.util.Timer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This <code>Callable</code> will be used by <code>getXMLDocuments</code>
 * It represents a single configuration resource (such as faces-config.xml) to be parsed into a DOM.
 */
public class ParseConfigResourceToDOMTask implements Callable<DocumentInfo> {

    /**
     * Name of the attribute added by ParseTask to indicate a {@link Document} instance as a representation of
     * <code>/WEB-INF/faces-config.xml</code>.
     */
    public static final String WEB_INF_MARKER = "org.glassfish.mojarra.webinf";

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    private static final String EMPTY_FACES_CONFIG = "org/glassfish/mojarra/empty-faces-config.xml";
    private static final String FACES_CONFIG_TAGNAME = "faces-config";
    private static final String FACELET_TAGLIB_TAGNAME = "facelet-taglib";

    /**
     * The namespaces a configuration document may legitimately declare, used to recognize one which was written with the
     * wrong scheme.
     */
    private static final Set<String> KNOWN_NAMESPACES = Set.of(
            XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
            JAKARTAEE_SCHEMA_DEFAULT_NS,
            JAVAEE_SCHEMA_DEFAULT_NS,
            JAVAEE_SCHEMA_LEGACY_DEFAULT_NS,
            FACES_CONFIG_1_X_DEFAULT_NS);

    /**
     * Stylesheet to convert 1.0 and 1.1 based faces-config documents to our private 1.1 schema for validation.
     */
    private static final String FACES_TO_1_1_PRIVATE_XSL = "/org/glassfish/mojarra/faces1_0-1_1toSchema.xsl";

    private final ServletContext servletContext;
    private final URI documentURI;
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
                documentNS = CURRENT_NAMESPACE;
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
            warnAboutNamespacesWhichDifferOnlyInScheme(configDocument);

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

                returnDoc = validateDocument(
                    findMatchingSchema(documentNS, getVersion(documentElement), documentElement.getLocalName()),
                    domSource);
                break;
            }

            default:
                // Assume a 1.0 or 1.1 faces-config in which case we need to transform it to reference a special 1.1 schema
                // before validating.

                returnDoc = validateDocument(
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

    /**
     * <p>
     * Returns <code>null</code> when the version is not one this release ships a schema for, which skips validation of
     * that document rather than failing the deployment. A library on the classpath may legitimately declare a newer
     * configuration version than the runtime knows.
     * </p>
     */
    private Schema findMatchingSchema(String documentNS, String version, String localName) {
        try {
            return DbfFactory.getSchema(servletContext, documentNS, version, localName);
        } catch (ConfigurationException e) {
            LOGGER.log(WARNING, "faces.config.schema.unknown", new Object[] { documentURI, e.getMessage() });
            return null;
        }
    }

    private Document validateDocument(Schema schema, DOMSource domSource) throws Exception {
        validate(schema, domSource);
        return (Document) domSource.getNode();
    }

    private Document validateDocument(Schema schema, DOMResult domResult) throws Exception {
        validate(schema, new DOMSource(domResult.getNode()));
        return (Document) domResult.getNode();
    }

    /**
     * <p>
     * A namespace is compared as an exact string and is never resolved, so writing <code>https</code> where the
     * namespace says <code>http</code> declares an entirely different namespace. The schema then rejects everything in
     * it, naming the attribute or element rather than the declaration which actually went wrong, which is a hard error
     * to read. This says what happened instead.
     * </p>
     */
    private void warnAboutNamespacesWhichDifferOnlyInScheme(Document document) {
        NamedNodeMap attributes = document.getDocumentElement().getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attribute = (Attr) attributes.item(i);

            if (!XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI())) {
                continue;
            }

            String declared = attribute.getValue();
            String withOtherScheme = withOtherScheme(declared);

            if (!KNOWN_NAMESPACES.contains(declared) && KNOWN_NAMESPACES.contains(withOtherScheme)) {
                LOGGER.log(WARNING, "faces.config.namespace.wrong_scheme",
                        new Object[] { documentURI, attribute.getName(), declared, withOtherScheme });
            }
        }
    }

    private static String withOtherScheme(String namespace) {
        if (namespace.startsWith("http://")) {
            return "https://" + namespace.substring("http://".length());
        }

        if (namespace.startsWith("https://")) {
            return "http://" + namespace.substring("https://".length());
        }

        return namespace;
    }

    /**
     * <p>
     * Reports every way in which the document departs from its schema, rather than stopping at the first one and taking
     * the deployment down with it. A configuration file which a previous release accepted has to keep deploying.
     * </p>
     */
    private void validate(Schema schema, DOMSource domSource) throws Exception {
        if (schema == null) {
            return;
        }

        Validator validator = schema.newValidator();
        validator.setErrorHandler(new SchemaViolationReporter());
        validator.validate(domSource);
    }

    /**
     * Logs each schema violation and lets the validation continue, so that one run reports all of them.
     */
    private final class SchemaViolationReporter implements ErrorHandler {

        @Override
        public void warning(SAXParseException exception) {
            // Not a violation, and the schemas themselves produce these.
        }

        @Override
        public void error(SAXParseException exception) {
            LOGGER.log(WARNING, "faces.config.schema.violation",
                    new Object[] { documentURI, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage() });
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
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

    private boolean isNonFacesConfigDocument(String rootElementTagName) {
        return !FACES_CONFIG_TAGNAME.equals(rootElementTagName) && !FACELET_TAGLIB_TAGNAME.equals(rootElementTagName);
    }

    private Document getEmptyFacesConfig(DocumentBuilder documentBuilder) throws SAXException, IOException {
        return documentBuilder.parse(
                new InputSource(getInputStream(getClass().getClassLoader().getResource(EMPTY_FACES_CONFIG))));
    }

}
