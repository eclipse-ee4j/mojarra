/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package com.sun.faces;

import java.nio.charset.StandardCharsets;

import com.sun.faces.config.manager.FacesSchema;

import jakarta.faces.render.RenderKitFactory;

/**
 * This class contains literal strings used throughout the Faces RI.
 */
public class RIConstants {

    /**
     * Used to add uniqueness to the names.
     */
    public static final String FACES_PREFIX = "com.sun.faces.";

    public static final String HTML_BASIC_RENDER_KIT = FACES_PREFIX + RenderKitFactory.HTML_BASIC_RENDER_KIT;

    public static final String SAVESTATE_FIELD_DELIMITER = "~";
    public static final String SAVESTATE_FIELD_MARKER = SAVESTATE_FIELD_DELIMITER + FACES_PREFIX + "saveStateFieldMarker" + SAVESTATE_FIELD_DELIMITER;

    public static final String SAVED_STATE = FACES_PREFIX + "savedState";

    /*
     * <p>TLV Resource Bundle Location </p>
     */
    public static final String TLV_RESOURCE_LOCATION = FACES_PREFIX + "resources.Resources";

    public static final String NO_VALUE = "";

    public static final Class<?>[] EMPTY_CLASS_ARGS = new Class[0];
    public static final Object[] EMPTY_METH_ARGS = new Object[0];

    /**
     * <p>
     * ResponseWriter Content Types and Encoding
     * </p>
     */
    public static final String HTML_CONTENT_TYPE = "text/html";
    public static final String XHTML_CONTENT_TYPE = "application/xhtml+xml";
    public static final String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    public static final String TEXT_XML_CONTENT_TYPE = "text/xml";
    public static final String ALL_MEDIA = "*/*";
    public static final String CHAR_ENCODING = StandardCharsets.UTF_8.name();
    public static final String FACELETS_ENCODING_KEY = "facelets.Encoding";
    public static final String DEFAULT_LIFECYCLE = FACES_PREFIX + "DefaultLifecycle";
    public static final String DEFAULT_STATEMANAGER = FACES_PREFIX + "DefaultStateManager";

    public static final String ERROR_PAGE_PRESENT_KEY_NAME = FACES_PREFIX + "errorPagePresent";

    public static final String VIEWID_KEY_NAME = FACES_PREFIX + "viewId";

    public static final String PUSH_RESOURCE_URLS_KEY_NAME = FACES_PREFIX + "resourceUrls";

    /**
     * Marker used when saving the list of component adds and removes.
     */
    public static final String DYNAMIC_ACTIONS = FACES_PREFIX + "DynamicActions";

    /**
     * Marker attached to a component that has dynamic children.
     */
    public static final String DYNAMIC_CHILD_COUNT = FACES_PREFIX + "DynamicChildCount";

    /**
     * Marker attached to a component that was added dynamically.
     */
    public static final String DYNAMIC_COMPONENT = FACES_PREFIX + "DynamicComponent";

    /**
     * Present in the attrs of UIViewRoot iff the tree has one or more dynamic modifications
     */
    public static final String TREE_HAS_DYNAMIC_COMPONENTS = FACES_PREFIX + "TreeHasDynamicComponents";

    public static final String FLOW_DEFINITION_ID_SUFFIX = "-flow.xml";

    public static final int FLOW_DEFINITION_ID_SUFFIX_LENGTH = FLOW_DEFINITION_ID_SUFFIX.length();

    public static final String FLOW_IN_JAR_PREFIX = "META-INF/flows";

    public static final int FLOW_IN_JAR_PREFIX_LENGTH = FLOW_IN_JAR_PREFIX.length();

    public static final String FLOW_DISCOVERY_CDI_HELPER_BEAN_NAME = "csfFLOWDISCOVERYCDIHELPER";

    public static final String DOCUMENT_NAMESPACE = FacesSchema.Schemas.JAKARTAEE_SCHEMA_DEFAULT_NS;

    public static final String DOCUMENT_VERSION = "4.0";

    /**
     * Convenience key to store / get BeanManager.
     */
    public static final String CDI_BEAN_MANAGER = FACES_PREFIX + "cdi.BeanManager";

    /**
     * Convenience key in App map and FacesContext map for spec version of faces-config.xml
     */
    public static final String FACES_CONFIG_VERSION = FACES_PREFIX + "facesConfigVersion";

    /**
     * Convenience key to temporarily store the set of annotated classes in the servlet context.
     */
    public static final String ANNOTATED_CLASSES = FACES_PREFIX + "AnnotatedClasses";

    /**
     * Key to annotate the mappings for the FacesServlet. Since servlet 3.0 the ConfigureListener
     * cannot access the servlet mappings because it is initialized by a TLD and it is programmatic.
     * So this key will store the mappings during the initialization.
     */
    public static final String FACES_SERVLET_MAPPINGS = FACES_PREFIX + "FacesServletMappings";

    /**
     * Key to annotate the registration for the FacesServlet. Since servlet 3.0 the ConfigureListener
     * cannot access the servlet registration because it is initialized by a TLD and it is programmatic.
     * So this key will store the registration during the initialization.
     */
    public static final String FACES_SERVLET_REGISTRATION = FACES_PREFIX + "FacesServletRegistration";

    private RIConstants() {
        throw new IllegalStateException();
    }
}
