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

package com.sun.faces.config.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.faces.config.ConfigurationException;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.servlet.ServletContext;

/**
 * <p>
 * Create and configure DocumentBuilderFactory instances.
 * </p>
 */
public class DbfFactory {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    private static final String AS_INSTALL_ROOT = "com.sun.aas.installRoot";

    private static final String AS_SCHEMA_DIR = System.getProperty(AS_INSTALL_ROOT) + File.separatorChar + "lib" + File.separatorChar + "schemas"
            + File.separatorChar;

    private static final String AS_DTD_DIR = System.getProperty(AS_INSTALL_ROOT) + File.separatorChar + "lib" + File.separatorChar + "dtds"
            + File.separatorChar;

    /**
     * Location of the facelet-taglib 2.0 Schema
     */
    private static final String FACELET_TAGLIB_2_0_XSD = "/com/sun/faces/web-facelettaglibrary_2_0.xsd";

    /**
     * Location of the facelet-taglib 2.2 Schema
     */
    private static final String FACELET_TAGLIB_2_2_XSD = "/com/sun/faces/web-facelettaglibrary_2_2.xsd";

    /**
     * Location of the Faces 2.0 Schema
     */
    private static final String FACES_2_0_XSD = "/com/sun/faces/web-facesconfig_2_0.xsd";

    /**
     * Location of the Faces 2.1 Schema
     */
    private static final String FACES_2_1_XSD = "/com/sun/faces/web-facesconfig_2_1.xsd";

    /**
     * Location of the Faces 2.2 Schema
     */
    private static final String FACES_2_2_XSD = "/com/sun/faces/web-facesconfig_2_2.xsd";

    /**
     * Location of the Faces 2.3 Schema
     */
    private static final String FACES_2_3_XSD = "/com/sun/faces/web-facesconfig_2_3.xsd";

    /**
     * Location of the Faces 1.2 Schema
     */
    private static final String FACES_1_2_XSD = "/com/sun/faces/web-facesconfig_1_2.xsd";

    /**
     * Location of the Faces private 1.1 Schema
     */
    private static final String FACES_1_1_XSD = "/com/sun/faces/web-facesconfig_1_1.xsd";

    /**
     * Location of the facelet taglib xsd within GlassFish.
     */
    private static final String FACELET_TAGLIB_2_0_XSD_FILE = AS_SCHEMA_DIR + "web-facelettaglibrary_2_0.xsd";

    /**
     * Location of the facelet taglib xsd within GlassFish.
     */
    private static final String FACELET_TAGLIB_2_2_XSD_FILE = AS_SCHEMA_DIR + "web-facelettaglibrary_2_2.xsd";

    /**
     * Location of the faces 2.0 xsd within GlassFish.
     */
    private static final String FACES_2_0_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_2_0.xsd";

    /**
     * Location of the faces 2.1 xsd within GlassFish.
     */
    private static final String FACES_2_1_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_2_1.xsd";

    /**
     * Location of the faces 2.1 xsd within GlassFish.
     */
    private static final String FACES_2_2_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_2_2.xsd";

    /**
     * Location of the faces 2.3 xsd within GlassFish.
     */
    private static final String FACES_2_3_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_2_3.xsd";

    /**
     * Location of the faces 1.2 xsd within GlassFish.
     */
    private static final String FACES_1_2_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_1_2.xsd";

    /**
     * EntityResolver
     */
    public static final EntityResolver FACES_ENTITY_RESOLVER = new FacesEntityResolver();

    /**
     * The constant that points to the schema map (in the servlet context).
     */
    private static final String SCHEMA_MAP = "com.sun.faces.config.schemaMap";

    public enum FacesSchema {

        FACES_20, FACES_21, FACES_22, FACES_23, FACES_12, FACES_11, FACELET_TAGLIB_20, FACELET_TAGLIB_22;
    }

    /**
     * ErrorHandler
     */
    public static final FacesErrorHandler FACES_ERROR_HANDLER = new FacesErrorHandler();

    // ---------------------------------------------------------- Public Methods

    public static DocumentBuilderFactory getFactory() {

        DocumentBuilderFactory factory = Util.createDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        return factory;

    }

    // ----------------------------------------------------------- Inner Classes

    private static class FacesEntityResolver extends DefaultHandler implements LSResourceResolver {

        /**
         * <p>
         * Contains associations between grammar name and the physical resource.
         * </p>
         */
        private static final String[][] DTD_SCHEMA_INFO = {
                { "web-facesconfig_1_0.dtd", "/com/sun/faces/web-facesconfig_1_0.dtd", AS_DTD_DIR + "web-facesconfig_1_0.dtd" },
                { "web-facesconfig_1_1.dtd", "/com/sun/faces/web-facesconfig_1_1.dtd", AS_DTD_DIR + "web-facesconfig_1_1.dtd" },
                { "web-facesconfig_2_0.xsd", FACES_2_0_XSD, FACES_2_0_XSD_FILE }, { "web-facesconfig_2_1.xsd", FACES_2_1_XSD, FACES_2_1_XSD_FILE },
                { "web-facesconfig_2_2.xsd", FACES_2_2_XSD, FACES_2_2_XSD_FILE }, { "web-facesconfig_2_3.xsd", FACES_2_3_XSD, FACES_2_3_XSD_FILE },
                { "facelet-taglib_1_0.dtd", "/com/sun/faces/facelet-taglib_1_0.dtd", null },
                { "web-facelettaglibrary_2_0.xsd", FACELET_TAGLIB_2_0_XSD, FACELET_TAGLIB_2_0_XSD_FILE },
                { "web-facesconfig_1_2.xsd", FACES_1_2_XSD, FACES_1_2_XSD_FILE }, { "web-facesconfig_1_1.xsd", FACES_1_1_XSD, null },
                { "javaee_5.xsd", "/com/sun/faces/javaee_5.xsd", AS_SCHEMA_DIR + "javaee_5.xsd" },
                { "javaee_6.xsd", "/com/sun/faces/javaee_6.xsd", AS_SCHEMA_DIR + "javaee_6.xsd" },
                { "javaee_7.xsd", "/com/sun/faces/javaee_7.xsd", AS_SCHEMA_DIR + "javaee_7.xsd" },
                { "javaee_8.xsd", "/com/sun/faces/javaee_8.xsd", AS_SCHEMA_DIR + "javaee_8.xsd" },
                { "javaee_web_services_client_1_2.xsd", "/com/sun/faces/javaee_web_services_client_1_2.xsd",
                        AS_SCHEMA_DIR + "javaee_web_services_client_1_2.xsd" },
                { "javaee_web_services_client_1_3.xsd", "/com/sun/faces/javaee_web_services_client_1_3.xsd",
                        AS_SCHEMA_DIR + "javaee_web_services_client_1_3.xsd" },
                { "javaee_web_services_client_1_4.xsd", "/com/sun/faces/javaee_web_services_client_1_4.xsd",
                        AS_SCHEMA_DIR + "javaee_web_services_client_1_4.xsd" },
                { "xml.xsd", "/com/sun/faces/xml.xsd", AS_SCHEMA_DIR + "xml.xsd" },
                { "datatypes.dtd", "/com/sun/faces/datatypes.dtd", AS_SCHEMA_DIR + "datatypes.dtd" },
                { "XMLSchema.dtd", "/com/sun/faces/XMLSchema.dtd", AS_SCHEMA_DIR + "XMLSchema.dtd" } };

        /**
         * <p>
         * Contains mapping between grammar name and the local URL to the physical resource.
         * </p>
         */
        private HashMap<String, String> entities = new HashMap<>(12, 1.0f);

        // -------------------------------------------------------- Constructors

        public FacesEntityResolver() {

            // Add mappings between last segment of system ID and
            // the expected local physical resource. If the resource
            // cannot be found, then rely on default entity resolution
            // and hope a firewall isn't in the way or a proxy has
            // been configured
            for (String[] aDTD_SCHEMA_INFO : DTD_SCHEMA_INFO) {
                URL url = this.getClass().getResource(aDTD_SCHEMA_INFO[1]);
                if (url == null) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "jsf.config.cannot_resolve_entities", new Object[] { aDTD_SCHEMA_INFO[1], aDTD_SCHEMA_INFO[0] });
                    }
                    // the resource isn't available on the classpath, so
                    // assume that we're running within a GF environment
                    String path = aDTD_SCHEMA_INFO[2];
                    if (path != null) {
                        File f = new File(path);
                        if (f.exists()) {
                            try {
                                url = f.toURI().toURL();
                            } catch (MalformedURLException mue) {
                                if (LOGGER.isLoggable(Level.SEVERE)) {
                                    LOGGER.log(Level.SEVERE, mue.toString(), mue);
                                }
                            }
                            if (url == null) {
                                if (LOGGER.isLoggable(Level.FINE)) {
                                    LOGGER.log(Level.FINE, "jsf.config.cannot_resolve_entities", new Object[] { aDTD_SCHEMA_INFO[1], aDTD_SCHEMA_INFO[2] });
                                }
                            } else {
                                entities.put(aDTD_SCHEMA_INFO[0], url.toString());
                            }
                        }

                    }
                } else {
                    entities.put(aDTD_SCHEMA_INFO[0], url.toString());
                }
            }

        } // END JsfEntityResolver

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
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Unknown entity, deferring to superclass.");
                }

                try {
                    source = super.resolveEntity(publicId, systemId);
                } catch (IOException | SAXException e) {
                    throw new SAXException(e);
                }

            } else {

                try {
                    source = new InputSource(new URL(entityURL).openStream());
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, "jsf.config.cannot_create_inputsource", entityURL);
                    }
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

        } // END resolveEntity

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

    } // END FacesEntityResolver

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
    } // END FacesErrorHandler

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
        public void setCharacterStream(Reader characterStream) {
        }

        @Override
        public InputStream getByteStream() {
            return in;
        }

        @Override
        public void setByteStream(InputStream byteStream) {
        }

        @Override
        public String getStringData() {
            return null;
        }

        @Override
        public void setStringData(String stringData) {
        }

        @Override
        public String getSystemId() {
            return null;
        }

        @Override
        public void setSystemId(String systemId) {
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public void setPublicId(String publicId) {
        }

        @Override
        public String getBaseURI() {
            return null;
        }

        @Override
        public void setBaseURI(String baseURI) {
        }

        @Override
        public String getEncoding() {
            return null;
        }

        @Override
        public void setEncoding(String encoding) {
        }

        @Override
        public boolean getCertifiedText() {
            return false;
        }

        @Override
        public void setCertifiedText(boolean certifiedText) {
        }
    }

    /**
     * Get the schema for the given schema id.
     *
     * @param servletContext the backing servlet context.
     * @param schemaId the schema id.
     * @return the schema, or null if not found.
     */
    public static Schema getSchema(ServletContext servletContext, FacesSchema schemaId) {
        Map<FacesSchema, Schema> schemaMap = getSchemaMap(servletContext);
        if (!schemaMap.containsKey(schemaId)) {
            loadSchema(schemaMap, schemaId);
        }
        return schemaMap.get(schemaId);
    }

    /**
     * Get the schema map from the servlet context (or create it).
     *
     * @param servletContext the servlet context.
     * @return the schema map.
     */
    private static Map<FacesSchema, Schema> getSchemaMap(ServletContext servletContext) {
        Map<FacesSchema, Schema> schemaMap = (Map<FacesSchema, Schema>) servletContext.getAttribute(SCHEMA_MAP);

        if (schemaMap == null) {
            synchronized (servletContext) {
                schemaMap = Collections.synchronizedMap(new EnumMap<FacesSchema, Schema>(FacesSchema.class));
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

    /**
     * Load the schema for the given schema id.
     *
     * @param schemaMap the schema map.
     * @param schemaId the schema id.
     */
    private static void loadSchema(Map<FacesSchema, Schema> schemaMap, FacesSchema schemaId) {
        URL url;
        URLConnection conn;
        InputStream in;
        SchemaFactory factory;
        File f;
        Schema schema;

        try {
            switch (schemaId) {
            case FACES_12:
                url = DbfFactory.class.getResource(FACES_1_2_XSD);
                if (url == null) {
                    // try to load from the file
                    f = new File(FACES_1_2_XSD_FILE);
                    if (!f.exists()) {
                        throw new IllegalStateException("Unable to find web-facesconfig_1_2.xsd");
                    }
                    url = f.toURI().toURL();
                }
                conn = url.openConnection();
                conn.setUseCaches(false);
                in = conn.getInputStream();
                factory = Util.createSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setResourceResolver((LSResourceResolver) DbfFactory.FACES_ENTITY_RESOLVER);
                schema = factory.newSchema(new StreamSource(in));
                schemaMap.put(schemaId, schema);
                break;
            case FACES_11:
                url = DbfFactory.class.getResource(FACES_1_1_XSD);
                conn = url.openConnection();
                conn.setUseCaches(false);
                in = conn.getInputStream();
                factory = Util.createSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setResourceResolver((LSResourceResolver) DbfFactory.FACES_ENTITY_RESOLVER);
                schema = factory.newSchema(new StreamSource(in));
                schemaMap.put(schemaId, schema);
                break;
            case FACES_21:
                url = DbfFactory.class.getResource(FACES_2_1_XSD);
                if (url == null) {
                    // try to load from the file
                    f = new File(FACES_2_1_XSD_FILE);
                    if (!f.exists()) {
                        throw new IllegalStateException("Unable to find web-facesconfig_2_1.xsd");
                    }
                    url = f.toURI().toURL();
                }
                conn = url.openConnection();
                conn.setUseCaches(false);
                in = conn.getInputStream();
                factory = Util.createSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setResourceResolver((LSResourceResolver) DbfFactory.FACES_ENTITY_RESOLVER);
                schema = factory.newSchema(new StreamSource(in));
                schemaMap.put(schemaId, schema);
                break;
            case FACES_22:
                url = DbfFactory.class.getResource(FACES_2_2_XSD);
                if (url == null) {
                    // try to load from the file
                    f = new File(FACES_2_2_XSD_FILE);
                    if (!f.exists()) {
                        throw new IllegalStateException("Unable to find web-facesconfig_2_2.xsd");
                    }
                    url = f.toURI().toURL();
                }
                conn = url.openConnection();
                conn.setUseCaches(false);
                in = conn.getInputStream();
                factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setResourceResolver((LSResourceResolver) DbfFactory.FACES_ENTITY_RESOLVER);
                schema = factory.newSchema(new StreamSource(in));
                schemaMap.put(schemaId, schema);
                break;
            case FACES_23:
                url = DbfFactory.class.getResource(FACES_2_3_XSD);
                if (url == null) {
                    // try to load from the file
                    f = new File(FACES_2_3_XSD_FILE);
                    if (!f.exists()) {
                        throw new IllegalStateException("Unable to find web-facesconfig_2_3.xsd");
                    }
                    url = f.toURI().toURL();
                }
                conn = url.openConnection();
                conn.setUseCaches(false);
                in = conn.getInputStream();
                factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setResourceResolver((LSResourceResolver) DbfFactory.FACES_ENTITY_RESOLVER);
                schema = factory.newSchema(new StreamSource(in));
                schemaMap.put(schemaId, schema);
                break;
            case FACES_20:
                url = DbfFactory.class.getResource(FACES_2_0_XSD);
                if (url == null) {
                    // try to load from the file
                    f = new File(FACES_2_0_XSD_FILE);
                    if (!f.exists()) {
                        throw new IllegalStateException("Unable to find web-facesconfig_2_0.xsd");
                    }
                    url = f.toURI().toURL();
                }
                conn = url.openConnection();
                conn.setUseCaches(false);
                in = conn.getInputStream();
                factory = Util.createSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setResourceResolver((LSResourceResolver) DbfFactory.FACES_ENTITY_RESOLVER);
                schema = factory.newSchema(new StreamSource(in));
                schemaMap.put(schemaId, schema);
                break;
            case FACELET_TAGLIB_20:
                url = DbfFactory.class.getResource(FACELET_TAGLIB_2_0_XSD);
                if (url == null) {
                    // try to load from the file
                    f = new File(FACELET_TAGLIB_2_0_XSD_FILE);
                    if (!f.exists()) {
                        throw new IllegalStateException("Unable to find web-facelettaglibrary_2_0.xsd");
                    }
                    url = f.toURI().toURL();
                }
                conn = url.openConnection();
                conn.setUseCaches(false);
                in = conn.getInputStream();
                factory = Util.createSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setResourceResolver((LSResourceResolver) DbfFactory.FACES_ENTITY_RESOLVER);
                schema = factory.newSchema(new StreamSource(in));
                schemaMap.put(schemaId, schema);
                break;
            case FACELET_TAGLIB_22:
                url = DbfFactory.class.getResource(FACELET_TAGLIB_2_2_XSD);
                if (url == null) {
                    // try to load from the file
                    f = new File(FACELET_TAGLIB_2_2_XSD_FILE);
                    if (!f.exists()) {
                        throw new IllegalStateException("Unable to find web-facelettaglibrary_2_2.xsd");
                    }
                    url = f.toURI().toURL();
                }
                conn = url.openConnection();
                conn.setUseCaches(false);
                in = conn.getInputStream();
                factory = Util.createSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setResourceResolver((LSResourceResolver) DbfFactory.FACES_ENTITY_RESOLVER);
                schema = factory.newSchema(new StreamSource(in));
                schemaMap.put(schemaId, schema);
                break;
            default:
                throw new ConfigurationException("Unrecognized Faces Version: " + schemaId.toString());
            }
        } catch (IllegalStateException | IOException | SAXException | ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }
}
