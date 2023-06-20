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

package com.sun.faces.config.manager;

import static com.sun.faces.util.Util.createDocumentBuilderFactory;
import static com.sun.faces.util.Util.createSchemaFactory;
import static java.util.Collections.synchronizedMap;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.faces.config.ConfigurationException;
import com.sun.faces.util.FacesLogger;

import jakarta.servlet.ServletContext;


/**
 * <p>Create and configure DocumentBuilderFactory instances.</p>
 */
public class DbfFactory {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * The constant that points to the schema map (in the servlet context).
     */
    private static final String SCHEMA_MAP = "com.sun.faces.config.schemaMap";

    /**
     * EntityResolver
     */
    public static final FacesEntityResolver FACES_ENTITY_RESOLVER = new FacesEntityResolver();

    /**
     * ErrorHandler
     */
    public static final FacesErrorHandler FACES_ERROR_HANDLER = new FacesErrorHandler();



    // ---------------------------------------------------------- Public Methods


    public static DocumentBuilderFactory getFactory() {
        DocumentBuilderFactory factory = createDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        return factory;
    }


    // ----------------------------------------------------------- Inner Classes



    private static class FacesEntityResolver extends DefaultHandler implements LSResourceResolver {


        /**
         * Contains mapping between grammar name and the local URL to the
         * physical resource.
         */
        private HashMap<String, String> entities = new HashMap<>(12, 1.0f);

        // -------------------------------------------------------- Constructors


        public FacesEntityResolver() {

            // Add mappings between last segment of system ID and the expected local physical resource. If the resource
            // cannot be found, then rely on default entity resolution and hope a firewall isn't in the way or a proxy has
            // been configured
            for (String[] schemaInfo : FacesSchema.Schemas.DTD_SCHEMA_INFO) {
                URL schemaUrl = getClass().getResource(schemaInfo[1]);
                if (schemaUrl == null) {
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, "faces.config.cannot_resolve_entities", new Object[] { schemaInfo[1], schemaInfo[0] });
                    }

                    // The resource isn't available on the classpath, so
                    // assume that we're running within a GF environment
                    String schemaPath = schemaInfo[2];
                    if (schemaPath != null) {
                        File schemaFile = new File(schemaPath);
                        if (schemaFile.exists()) {
                            try {
                                schemaUrl = schemaFile.toURI().toURL();
                            } catch (MalformedURLException mue) {
                                LOGGER.log(SEVERE, mue, mue::toString);
                            }

                            if (schemaUrl == null) {
                                if (LOGGER.isLoggable(FINE)) {
                                    LOGGER.log(FINE, "faces.config.cannot_resolve_entities", new Object[] { schemaInfo[1], schemaInfo[2] });
                                }
                            } else {
                                entities.put(schemaInfo[0], schemaUrl.toString());
                            }
                        }

                    }
                } else {
                    entities.put(schemaInfo[0], schemaUrl.toString());
                }
            }
        }


        // ----------------------------------------- Methods from DefaultHandler


        /**
         * <p>
         * Resolves the physical resource using the last segment of the <code>systemId</code> (e.g.
         * http://java.sun.com/dtds/web-facesconfig_1_1.dtd, the last segment would be web-facesconfig_1_1.dtd). If a mapping
         * cannot be found for the segment, then defer to the <code>DefaultHandler</code> for resolution.
         * </p>
         */
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException {

            // publicId is ignored. Resolution performed using
            // the systemId.

            // If no system ID, defer to superclass
            if (systemId == null) {
                InputSource result;
                try {
                    result = super.resolveEntity(publicId, null);
                } catch (IOException | SAXException e) {
                    throw new SAXException(e);
                }
                return result;
            }

            String grammarName = systemId.substring(systemId.lastIndexOf('/') + 1);
            String entityURL = entities.get(grammarName);

            InputSource source;
            if (entityURL == null) {
                // we don't have a registered mapping, so defer to our
                // superclass for resolution
                LOGGER.log(FINE, "Unknown entity, deferring to superclass.");

                try {
                    source = super.resolveEntity(publicId, systemId);
                } catch (IOException | SAXException e) {
                    throw new SAXException(e);
                }

            } else {

                try {
                    source = new InputSource(new URL(entityURL).openStream());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "faces.config.cannot_create_inputsource", entityURL);
                    source = null;
                }
            }

            // Set the System ID of the InputSource with the URL of the local
            // resource - necessary to prevent parsing errors
            if (source != null) {
                source.setSystemId(entityURL);

                if (publicId != null) {
                    source.setPublicId(publicId);
                }
            }

            return source;
       }

       @Override
       public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
           try {
               InputSource source = resolveEntity(publicId, systemId);
               if (source != null) {
                   return new Input(source.getByteStream());
               }
           } catch (Exception e) {
               throw new ConfigurationException(e);
           }
           return null;
       }
    }


    private static class FacesErrorHandler implements ErrorHandler {
        @Override
        public void warning(SAXParseException exception) throws SAXException {
            // do nothing
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
    }


    private static final class Input implements LSInput {
        InputStream in;
        public Input(InputStream in) {
           this.in = in;
        }
        @Override
        public Reader getCharacterStream() {
            return null;
        }

        @Override
        public void setCharacterStream(Reader characterStream) { }

        @Override
        public InputStream getByteStream() {
            return in;
        }

        @Override
        public void setByteStream(InputStream byteStream) { }

        @Override
        public String getStringData() {
            return null;
        }

        @Override
        public void setStringData(String stringData) { }

        @Override
        public String getSystemId() {
            return null;
        }

        @Override
        public void setSystemId(String systemId) { }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public void setPublicId(String publicId) { }

        @Override
        public String getBaseURI() {
            return null;
        }

        @Override
        public void setBaseURI(String baseURI) { }

        @Override
        public String getEncoding() {
            return null;
        }

        @Override
        public void setEncoding(String encoding) { }

        @Override
        public boolean getCertifiedText() {
            return false;
        }

        @Override
        public void setCertifiedText(boolean certifiedText) { }
    }

    /**
     * Get the schema for the given schema id.
     *
     * @param servletContext the backing servlet context.
     * @param documentNS namespace of the document
     * @param version version attribute of the root tag, if any
     * @param localName name of the root tag
     * @return the schema, or null if not found.
     */
    public static Schema getSchema(ServletContext servletContext, String documentNS, String version, String localName) {
        return getSchema(servletContext, FacesSchema.fromDocumentId(documentNS, version, localName));
    }

    /**
     * Get the schema for the given schema id.
     *
     * @param servletContext the backing servlet context.
     * @param schemaId the schema id.
     * @return the schema, or null if not found.
     */
    public static Schema getSchema(ServletContext servletContext, FacesSchema schemaId) {
        return getSchemaMap(servletContext).computeIfAbsent(schemaId, e -> schemaId.loadSchema());
    }

    /**
     * Get the schema map from the servlet context (or create it).
     *
     * @param servletContext the servlet context.
     * @return the schema map.
     */
    @SuppressWarnings("unchecked")
    private static Map<FacesSchema, Schema> getSchemaMap(ServletContext servletContext) {
        Map<FacesSchema, Schema> schemaMap = (Map<FacesSchema, Schema>) servletContext.getAttribute(SCHEMA_MAP);

        if (schemaMap == null) {
            synchronized (servletContext) {
                schemaMap = synchronizedMap(new EnumMap<>(FacesSchema.class));
                servletContext.setAttribute(SCHEMA_MAP, schemaMap);
            }
        }

        return schemaMap;
    }

    /**
     * Remove the schema map from the servlet context.
     *
     * @param servletContext the servlet context.
     */
    public static void removeSchemaMap(ServletContext servletContext) {
        servletContext.removeAttribute(SCHEMA_MAP);
    }

    public static Schema loadSchema(String resourceName, String fileName) throws SAXException, IOException {
        URL url = DbfFactory.class.getResource(resourceName);
        if (url == null) {
            // Try to load from the file
            File schemaFile = new File(fileName);
            if (!schemaFile.exists()) {
                throw new IllegalStateException("Unable to find " + resourceName);
            }
            url = schemaFile.toURI().toURL();
        }

        URLConnection urlConnection = url.openConnection();
        urlConnection.setUseCaches(false);
        try (InputStream in = urlConnection.getInputStream()) {
            SchemaFactory factory = createSchemaFactory(W3C_XML_SCHEMA_NS_URI);
            factory.setResourceResolver(FACES_ENTITY_RESOLVER);
            return factory.newSchema(new StreamSource(in));
        }
    }
}
