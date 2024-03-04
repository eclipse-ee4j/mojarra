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

import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_1_1_XSD;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_1_2_XSD;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_1_2_XSD_FILE;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_2_0_XSD;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_2_0_XSD_FILE;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_2_1_XSD;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_2_1_XSD_FILE;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_2_2_XSD;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_2_2_XSD_FILE;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_2_3_XSD;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_2_3_XSD_FILE;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_3_0_XSD;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_3_0_XSD_FILE;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_4_0_XSD;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_4_0_XSD_FILE;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_4_1_XSD;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_4_1_XSD_FILE;
import static com.sun.faces.config.manager.FacesSchema.Schemas.FACES_CONFIG_1_X_DEFAULT_NS;
import static com.sun.faces.config.manager.FacesSchema.Schemas.JAKARTAEE_SCHEMA_DEFAULT_NS;
import static com.sun.faces.config.manager.FacesSchema.Schemas.JAVAEE_SCHEMA_DEFAULT_NS;
import static com.sun.faces.config.manager.FacesSchema.Schemas.JAVAEE_SCHEMA_LEGACY_DEFAULT_NS;
import static java.io.File.separatorChar;

import java.io.IOException;

import javax.xml.validation.Schema;

import org.xml.sax.SAXException;

import com.sun.faces.config.ConfigurationException;

/**
 * Central place to store all data regarding Faces and related schemas and their namespaces.
 *
 * @author Arjan Tijms
 *
 */
public enum FacesSchema {

        // Enumeration constants associate a Faces version with its main configuration schemas

        // faces-config.xml
        FACES_11(FACES_1_1_XSD, ""),
        FACES_12(FACES_1_2_XSD, FACES_1_2_XSD_FILE),
        FACES_20(FACES_2_0_XSD, FACES_2_0_XSD_FILE),
        FACES_21(FACES_2_1_XSD, FACES_2_1_XSD_FILE),
        FACES_22(FACES_2_2_XSD, FACES_2_2_XSD_FILE),
        FACES_23(FACES_2_3_XSD, FACES_2_3_XSD_FILE),
        FACES_30(FACES_3_0_XSD, FACES_3_0_XSD_FILE),
        FACES_40(FACES_4_0_XSD, FACES_4_0_XSD_FILE),
        FACES_41(FACES_4_1_XSD, FACES_4_1_XSD_FILE),

        // taglib.xml
        FACELET_TAGLIB_20(Schemas.FACELET_TAGLIB_2_0_XSD, Schemas.FACELET_TAGLIB_2_0_XSD_FILE),
        FACELET_TAGLIB_22(Schemas.FACELET_TAGLIB_2_2_XSD, Schemas.FACELET_TAGLIB_2_2_XSD_FILE),
        FACELET_TAGLIB_23(Schemas.FACELET_TAGLIB_2_3_XSD, Schemas.FACELET_TAGLIB_2_3_XSD_FILE),
        FACELET_TAGLIB_30(Schemas.FACELET_TAGLIB_3_0_XSD, Schemas.FACELET_TAGLIB_3_0_XSD_FILE),
        FACELET_TAGLIB_40(Schemas.FACELET_TAGLIB_4_0_XSD, Schemas.FACELET_TAGLIB_4_0_XSD_FILE),
        FACELET_TAGLIB_41(Schemas.FACELET_TAGLIB_4_1_XSD, Schemas.FACELET_TAGLIB_4_1_XSD_FILE);

        /**
         * Constants for individual schema files
         */
        public static class Schemas {

            private static final String AS_INSTALL_ROOT = "com.sun.aas.installRoot";
            private static final String AS_SCHEMA_DIR = System.getProperty(AS_INSTALL_ROOT) + separatorChar + "lib" + separatorChar + "schemas" + separatorChar;
            private static final String AS_DTD_DIR = System.getProperty(AS_INSTALL_ROOT) + separatorChar + "lib" + separatorChar + "dtds" + separatorChar;

            public static final String FACES_CONFIG_1_X_DEFAULT_NS = "http://java.sun.com/JSF/Configuration";
            public static final String JAVAEE_SCHEMA_LEGACY_DEFAULT_NS = "http://java.sun.com/xml/ns/javaee";
            public static final String JAVAEE_SCHEMA_DEFAULT_NS = "http://xmlns.jcp.org/xml/ns/javaee";
            public static final String JAKARTAEE_SCHEMA_DEFAULT_NS = "https://jakarta.ee/xml/ns/jakartaee";

            public static final String FACELET_TAGLIB_2_0_XSD = "/com/sun/faces/web-facelettaglibrary_2_0.xsd";
            public static final String FACELET_TAGLIB_2_2_XSD = "/com/sun/faces/web-facelettaglibrary_2_2.xsd";
            public static final String FACELET_TAGLIB_2_3_XSD = "/com/sun/faces/web-facelettaglibrary_2_3.xsd";
            public static final String FACELET_TAGLIB_3_0_XSD = "/com/sun/faces/web-facelettaglibrary_3_0.xsd";
            public static final String FACELET_TAGLIB_4_0_XSD = "/com/sun/faces/web-facelettaglibrary_4_0.xsd";
            public static final String FACELET_TAGLIB_4_1_XSD = "/com/sun/faces/web-facelettaglibrary_4_1.xsd";

            public static final String FACES_1_2_XSD = "/com/sun/faces/web-facesconfig_1_2.xsd";
            public static final String FACES_1_1_XSD = "/com/sun/faces/web-facesconfig_1_1.xsd";
            public static final String FACES_2_0_XSD = "/com/sun/faces/web-facesconfig_2_0.xsd";
            public static final String FACES_2_1_XSD = "/com/sun/faces/web-facesconfig_2_1.xsd";
            public static final String FACES_2_2_XSD = "/com/sun/faces/web-facesconfig_2_2.xsd";
            public static final String FACES_2_3_XSD = "/com/sun/faces/web-facesconfig_2_3.xsd";
            public static final String FACES_3_0_XSD = "/com/sun/faces/web-facesconfig_3_0.xsd";
            public static final String FACES_4_0_XSD = "/com/sun/faces/web-facesconfig_4_0.xsd";
            public static final String FACES_4_1_XSD = "/com/sun/faces/web-facesconfig_4_1.xsd";

            public static final String FACELET_TAGLIB_2_0_XSD_FILE = AS_SCHEMA_DIR + "web-facelettaglibrary_2_0.xsd";
            public static final String FACELET_TAGLIB_2_2_XSD_FILE = AS_SCHEMA_DIR + "web-facelettaglibrary_2_2.xsd";
            public static final String FACELET_TAGLIB_2_3_XSD_FILE = AS_SCHEMA_DIR + "web-facelettaglibrary_2_3.xsd";
            public static final String FACELET_TAGLIB_3_0_XSD_FILE = AS_SCHEMA_DIR + "web-facelettaglibrary_3_0.xsd";
            public static final String FACELET_TAGLIB_4_0_XSD_FILE = AS_SCHEMA_DIR + "web-facelettaglibrary_4_0.xsd";
            public static final String FACELET_TAGLIB_4_1_XSD_FILE = AS_SCHEMA_DIR + "web-facelettaglibrary_4_1.xsd";

            // #### faces-config.xml XSDs within GlassFish

            public static final String FACES_1_2_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_1_2.xsd";
            public static final String FACES_2_0_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_2_0.xsd";
            public static final String FACES_2_1_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_2_1.xsd";
            public static final String FACES_2_2_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_2_2.xsd";
            public static final String FACES_2_3_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_2_3.xsd";
            public static final String FACES_3_0_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_3_0.xsd";
            public static final String FACES_4_0_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_4_0.xsd";
            public static final String FACES_4_1_XSD_FILE = AS_SCHEMA_DIR + "web-facesconfig_4_1.xsd";

            /**
             * Contains associations between grammar name and the physical resource.
             */
            public static final String[][] DTD_SCHEMA_INFO = {
                 {
                     "web-facesconfig_1_0.dtd",
                     "/com/sun/faces/web-facesconfig_1_0.dtd",
                     AS_DTD_DIR + "web-facesconfig_1_0.dtd"
                 },
                 {
                     "web-facesconfig_1_1.dtd",
                     "/com/sun/faces/web-facesconfig_1_1.dtd",
                     AS_DTD_DIR + "web-facesconfig_1_1.dtd"
                 },
                 {
                     "web-facesconfig_2_0.xsd",
                      FACES_2_0_XSD,
                      FACES_2_0_XSD_FILE
                 },
                 {
                     "web-facesconfig_2_1.xsd",
                      FACES_2_1_XSD,
                      FACES_2_1_XSD_FILE
                 },
                 {
                     "web-facesconfig_2_2.xsd",
                      FACES_2_2_XSD,
                      FACES_2_2_XSD_FILE
                 },
                 {
                     "web-facesconfig_2_3.xsd",
                      FACES_2_3_XSD,
                      FACES_2_3_XSD_FILE
                 },
                 {
                     "web-facesconfig_3_0.xsd",
                      FACES_3_0_XSD,
                      FACES_3_0_XSD_FILE
                 },
                 {
                     "web-facesconfig_4_0.xsd",
                      FACES_4_0_XSD,
                      FACES_4_0_XSD_FILE
                 },
                 {
                     "web-facesconfig_4_1.xsd",
                      FACES_4_1_XSD,
                      FACES_4_1_XSD_FILE
                 },
                 {
                     "facelet-taglib_1_0.dtd",
                     "/com/sun/faces/facelet-taglib_1_0.dtd",
                     null
                 },
                 {
                     "web-facelettaglibrary_2_0.xsd",
                      FACELET_TAGLIB_2_0_XSD,
                      FACELET_TAGLIB_2_0_XSD_FILE
                 },
                 {
                     "web-facelettaglibrary_2_2.xsd",
                      FACELET_TAGLIB_2_2_XSD,
                      FACELET_TAGLIB_2_2_XSD_FILE
                 },
                 {
                     "web-facelettaglibrary_2_3.xsd",
                      FACELET_TAGLIB_2_3_XSD,
                      FACELET_TAGLIB_2_3_XSD_FILE
                 },
                 {
                     "web-facelettaglibrary_3_0.xsd",
                      FACELET_TAGLIB_3_0_XSD,
                      FACELET_TAGLIB_3_0_XSD_FILE
                 },
                 {
                     "web-facelettaglibrary_4_0.xsd",
                      FACELET_TAGLIB_4_0_XSD,
                      FACELET_TAGLIB_4_0_XSD_FILE
                 },
                 {
                     "web-facelettaglibrary_4_1.xsd",
                      FACELET_TAGLIB_4_1_XSD,
                      FACELET_TAGLIB_4_1_XSD_FILE
                 },
                 {
                     "web-facesconfig_1_2.xsd",
                     FACES_1_2_XSD,
                     FACES_1_2_XSD_FILE
                 },
                 {
                     "web-facesconfig_1_1.xsd",
                     FACES_1_1_XSD,
                     null
                 },
                 {
                     "javaee_5.xsd",
                     "/com/sun/faces/javaee_5.xsd",
                     AS_SCHEMA_DIR + "javaee_5.xsd"
                 },
                 {
                     "javaee_6.xsd",
                     "/com/sun/faces/javaee_6.xsd",
                     AS_SCHEMA_DIR + "javaee_6.xsd"
                 },
                 {
                     "javaee_7.xsd",
                     "/com/sun/faces/javaee_7.xsd",
                     AS_SCHEMA_DIR + "javaee_7.xsd"
                 },
                 {
                     "javaee_8.xsd",
                     "/com/sun/faces/javaee_8.xsd",
                     AS_SCHEMA_DIR + "javaee_8.xsd"
                 },
                 {
                     "jakartaee_9.xsd",
                     "/com/sun/faces/jakartaee_9.xsd",
                     AS_SCHEMA_DIR + "jakartaee_9.xsd"
                 },
                 {
                     "jakartaee_10.xsd",
                     "/com/sun/faces/jakartaee_10.xsd",
                     AS_SCHEMA_DIR + "jakartaee_10.xsd"
                 },
                 {
                     "jakartaee_11.xsd",
                     "/com/sun/faces/jakartaee_11.xsd",
                     AS_SCHEMA_DIR + "jakartaee_11.xsd"
                 },
                 {
                     "javaee_web_services_client_1_2.xsd",
                     "/com/sun/faces/javaee_web_services_client_1_2.xsd",
                     AS_SCHEMA_DIR + "javaee_web_services_client_1_2.xsd"
                 },
                 {
                     "javaee_web_services_client_1_3.xsd",
                     "/com/sun/faces/javaee_web_services_client_1_3.xsd",
                     AS_SCHEMA_DIR + "javaee_web_services_client_1_3.xsd"
                 },
                 {
                     "javaee_web_services_client_1_4.xsd",
                     "/com/sun/faces/javaee_web_services_client_1_4.xsd",
                     AS_SCHEMA_DIR + "javaee_web_services_client_1_4.xsd"
                 },
                 {
                     "jakartaee_web_services_client_2_0.xsd",
                     "/com/sun/faces/jakartaee_web_services_client_2_0.xsd",
                     AS_SCHEMA_DIR + "jakartaee_web_services_client_2_0.xsd"
                 },
                 {
                     "xml.xsd",
                     "/com/sun/faces/xml.xsd",
                     AS_SCHEMA_DIR + "xml.xsd"
                 },
                 {
                     "datatypes.dtd",
                     "/com/sun/faces/datatypes.dtd",
                     AS_SCHEMA_DIR + "datatypes.dtd"
                 },
                 {
                     "XMLSchema.dtd",
                     "/com/sun/faces/XMLSchema.dtd",
                     AS_SCHEMA_DIR + "XMLSchema.dtd"
                 }
             };
        }


        private String resourceName;
        private String fileName;

        private FacesSchema(String resourceName, String fileName) {
            this.resourceName = resourceName;
            this.fileName = fileName;
        }

        /**
         * Maps a document id (coordinates) to a logical faces schema version.
         *
         * <p>
         * E.g. "https://jakarta.ee/xml/ns/jakartaee", "4.0", "faces-config" maps to <code>FACES_40</code>
         * </p>
         *
         * @param documentNS document's namespace
         * @param version document's version
         * @param localName document's root element
         * @return the matching faces schema
         */
        public static FacesSchema fromDocumentId(String documentNS, String version, String localName) {
            switch (documentNS) {
                case JAKARTAEE_SCHEMA_DEFAULT_NS: {
                    switch (version) {
                        case "4.1":
                            if ("facelet-taglib".equals(localName)) {
                                return FACELET_TAGLIB_41;
                            } else {
                                return FACES_41;
                            }
                        case "4.0":
                            if ("facelet-taglib".equals(localName)) {
                                return FACELET_TAGLIB_40;
                            } else {
                                return FACES_40;
                            }
                        case "3.0":
                            if ("facelet-taglib".equals(localName)) {
                                return FACELET_TAGLIB_30;
                            } else {
                                return FACES_30;
                            }
                        default:
                            throw new ConfigurationException("Unknown Schema version: " + version);
                    }
                }

                case JAVAEE_SCHEMA_DEFAULT_NS: {
                    switch (version) {
                        case "2.3":
                            if ("facelet-taglib".equals(localName)) {
                                return FACELET_TAGLIB_23;
                            } else {
                                return FACES_23;
                            }
                        case "2.2":
                            if ("facelet-taglib".equals(localName)) {
                                return FACELET_TAGLIB_22;
                            } else {
                                return FACES_22;
                            }
                        default:
                            throw new ConfigurationException("Unknown Schema version: " + version);
                    }
                }

                case JAVAEE_SCHEMA_LEGACY_DEFAULT_NS: {
                    switch (version) {
                        case "2.1":
                            if ("facelet-taglib".equals(localName)) {
                                return FACELET_TAGLIB_20;
                            } else {
                                return FACES_21;
                            }
                        case "2.0":
                            if ("facelet-taglib".equals(localName)) {
                                return FACELET_TAGLIB_20;
                            } else {
                                return FACES_20;
                            }
                        case "1.2":
                            return FACES_12;
                        default:
                            throw new ConfigurationException("Unknown Schema version: " + version);
                    }
                }

                case FACES_CONFIG_1_X_DEFAULT_NS: {
                    return FACES_11;
                }

                default:
                    return null;
            }

        }

        public Schema loadSchema() {
            try {
                return DbfFactory.loadSchema(resourceName, fileName);
            } catch (SAXException | IOException e) {
                throw new ConfigurationException(e);
            }
        }
    }