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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>This class manages properties common to <code>jsf-tools</code>
 * generators.</p>
 */
public class PropertyManager {

    /**
     * <p>The minimum required version of JSP.</p>
     */
    public static final String JSP_VERSION_PROPERTY = "jsp.version";

    /**
     * <p>The short-name of the generated tag library.</p>
     */
    public static final String TAGLIB_SHORT_NAME = "taglib.shortname";

    /**
     * <p>The display-name of the generated tag library.</p>
     */
    public static final String TAGLIB_DISPLAY_NAME = "taglib.displayname";

    /**
     * <p>The URI by which the tag library will be known by.</p>
     */
    public static final String TAGLIB_URI = "taglib.uri";

    /**
     * <p>The description of the tag library (may be <code>null</code>).</p>
     */
    public static final String TAGLIB_DESCRIPTION = "taglib.description";    

    /**
     * <p>The file name of the generated tag library descriptor.</p>
     */
    public static final String TAGLIB_FILE_NAME = "taglib.file.name";

    /**
     * <p>The filename of a file to be included as part of the tag
     * library generation process (may be <code>null</code>).</p>
     */
    public static final String TAGLIB_INCLUDE = "taglib.include";

    /**
     * <p>A comma separated list of renderer-types that must be excluded 
     * from taglib generation.</p>
     */
    public static final String TAGLIB_EXCLUDED_RENDERER_TYPES = "taglib.excludedRendererTypes";

    /**
     * <p>A copyright to be included at the beginning of any generated file
     * (may be <code>null</code>).</p>
     */
    public static final String COPYRIGHT = "copyright";

    /**
     * <p>The ID of the <code>RenderKit</code> for which the generation is
     * being performed.</p>
     */
    public static final String RENDERKIT_ID = "renderkit.id";

    /**
     * <p>The target package for any generated code.</p>
     */
    public static final String TARGET_PACKAGE = "target.package";

    /**
     * <p>The base directory in which all generated files are written to.</p>
     */
    public static final String BASE_OUTPUT_DIR = "base.output.dir";


    /**
     * <p>Known properties.</p>
     */
    private static final String[] VALID_PROPS = {
        JSP_VERSION_PROPERTY,
        TAGLIB_URI,
        TAGLIB_DESCRIPTION,
        TAGLIB_SHORT_NAME,
        COPYRIGHT,
        RENDERKIT_ID,
        TARGET_PACKAGE,
        TAGLIB_FILE_NAME,
        TAGLIB_INCLUDE,
        BASE_OUTPUT_DIR,
        TAGLIB_EXCLUDED_RENDERER_TYPES
    };

    /**
     * <p>Properties which may have no value.</p>
     */
    private static final String[] NULLABLE_PROPS = {
        COPYRIGHT,
        TAGLIB_DESCRIPTION,
        TAGLIB_INCLUDE,
        TAGLIB_EXCLUDED_RENDERER_TYPES
    };

    // Sort the arrays so we can use Arrays.binarySearch()
    static {
        Arrays.sort(VALID_PROPS);
        Arrays.sort(NULLABLE_PROPS);
    }


    /**
     * <p>The <code>Properties</code> object which backs this class.</p>
     */
    private Properties props;


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Constructs a new <code>PropertyManager</code> instance.</p>
     * @param props - properties
     */
    private PropertyManager(Properties props) {

        this.props = props;

    } // END PropertyManager


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Obtain a new <code>PropertyManager</code> instance backed by the
     * properies contained in the specified file.</p>
     * @param propertyFile - property file
     * @return a configured <code>PropertyManager</code> instance
     */
    public static PropertyManager newInstance(String propertyFile) {

        Properties props = new Properties();
        try {
            props.load(
                new BufferedInputStream(
                    new FileInputStream(new File(propertyFile))));

        } catch (Exception e) {
            throw new IllegalStateException(e.toString());
        }

        return new PropertyManager(props);

    } // END newInstance


    /**
     * <p>Return the property specified by <code>propertyName</code>.</p>
     * @param propertyName - the property name
     * @return the value of the property or <code>null</code> if no value
     *  is defined
     *
     * @throws IllegalArgumentException of <code>propertyName</code> isn't
     *  a known property
     * @throws IllegalStateException if <code>propertyName</code> illegally
     *  has no value
     */
    public String getProperty(String propertyName) {

        if (Arrays.binarySearch(VALID_PROPS, propertyName) < 0) {
            throw new IllegalArgumentException("Unknown Property '" +
                propertyName + '\'');
        }

        String propValue = props.getProperty(propertyName);

        if (propValue == null) {
            if (Arrays.binarySearch(NULLABLE_PROPS, propertyName) >= 0) {
                propValue = "";
            } else {
                throw new IllegalStateException("Property '" + propertyName +
                    "' must be defined.");
            }
        }

        return propValue.trim();

    } // END getProperty
    
    private Map<String, Boolean> excludedRenderers;
    
    public boolean isExcludeRenderer(String rendererType) {
        boolean result = false;
        if (null == excludedRenderers) {
            initializeExcludedRenderers();
        }
        result = excludedRenderers.containsKey(rendererType);
        
        return result;
    }
    
    private void initializeExcludedRenderers() {
        String excludedRenderersProp = getProperty("taglib.excludedRendererTypes");
        if (null == excludedRenderersProp) {
            excludedRenderers = Collections.emptyMap();
        } else {
            excludedRenderers = new HashMap<String, Boolean>();
            String [] toAdd = excludedRenderersProp.split(",");
            for (String cur : toAdd) {
                excludedRenderers.put(cur, Boolean.TRUE);
            }
        }
        
    }


    public String toString() {

        ByteArrayOutputStream propsOutput = new ByteArrayOutputStream();
        props.list(new PrintStream(propsOutput));
        return propsOutput.toString();

    } // END toString

}
