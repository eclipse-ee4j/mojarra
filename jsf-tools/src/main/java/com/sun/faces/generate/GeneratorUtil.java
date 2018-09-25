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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;

import com.sun.faces.config.DigesterFactory;
import com.sun.faces.config.beans.ComponentBean;
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.RenderKitBean;
import com.sun.faces.config.beans.RendererBean;
import com.sun.faces.config.rules.FacesConfigRuleSet;

/**
 * <p>
 * Utility methods that may be useful to all <code>Generators</code>.
 * </p>
 */
public class GeneratorUtil {

    private static final String PREFIX = "javax.faces.";

    // The set of unwrapper methods for primitives, keyed by the primitive type
    private static Map<String, String> UNWRAPPERS = new HashMap<String, String>();
    static {
        UNWRAPPERS.put("boolean", "booleanValue");
        UNWRAPPERS.put("byte", "byteValue");
        UNWRAPPERS.put("char", "charValue");
        UNWRAPPERS.put("double", "doubleValue");
        UNWRAPPERS.put("float", "floatValue");
        UNWRAPPERS.put("int", "intValue");
        UNWRAPPERS.put("long", "longValue");
        UNWRAPPERS.put("short", "shortValue");
    }

    // The set of wrapper classes for primitives, keyed by the primitive type
    private static Map<String, String> WRAPPERS = new HashMap<String, String>();
    static {
        WRAPPERS.put("boolean", "java.lang.Boolean");
        WRAPPERS.put("byte", "java.lang.Byte");
        WRAPPERS.put("char", "java.lang.Character");
        WRAPPERS.put("double", "java.lang.Double");
        WRAPPERS.put("float", "java.lang.Float");
        WRAPPERS.put("int", "java.lang.Integer");
        WRAPPERS.put("long", "java.lang.Long");
        WRAPPERS.put("short", "java.lang.Short");
    }

    // ---------------------------------------------------------- Public Methods

    public static String convertToPrimitive(String objectType) {

        return UNWRAPPERS.get(objectType);

    }

    public static String convertToObject(String primitiveType) {

        return WRAPPERS.get(primitiveType);

    }

    /**
     * <p>
     * Strip any "javax.faces." prefix from the beginning of the specified
     * identifier, and return it.
     * </p>
     *
     * @param identifier Identifier to be stripped
     * @return stripped Identifier
     */
    public static String stripJavaxFacesPrefix(String identifier) {

        if (identifier.startsWith(PREFIX)) {
            return (identifier.substring(PREFIX.length()));
        } else {
            return (identifier);
        }

    } // END stripJavaxFacesPrefix

    /**
     * Build the tag handler class name from componentFamily and rendererType.
     *
     * @param componentFamily the component family
     * @param rendererType the renderer type
     * @return tag handler class name
     */
    public static String makeTagClassName(String componentFamily, String rendererType) {

        if (componentFamily == null) {
            return null;
        }
        String tagClassName = componentFamily;
        if (rendererType != null) {
            if (!componentFamily.equals(rendererType)) {
                tagClassName = tagClassName + rendererType;
            }
        }
        return tagClassName + "Tag";

    } // END makeTagClassName

    /**
     *
     * @param configBean the FacesConfigBean
     * @return a SortedMap, where the keys are component-family String entries, and
     * the values are {@link com.sun.faces.config.beans.ComponentBean} instances
     * Only include components that do not have a base component type.
     */
    public static Map<String, ComponentBean> getComponentFamilyComponentMap(FacesConfigBean configBean) {

        TreeMap<String, ComponentBean> result = new TreeMap<String, ComponentBean>();
        ComponentBean component;
        ComponentBean[] components = configBean.getComponents();
        for (int i = 0, len = components.length; i < len; i++) {
            component = components[i];
            if (component == null) {
                throw new IllegalStateException("No Components Found");
            }
            if (component.isIgnore()) {
                continue;
            }
            if (component.getBaseComponentType() != null) {
                continue;
            }
            String componentFamily = component.getComponentFamily();

            result.put(componentFamily, component);
        }

        return result;

    } // END getComponentFamilyComponentMap

    public static Map<String, ArrayList<RendererBean>> getComponentFamilyRendererMap(FacesConfigBean configBean, String renderKitId) {

        RenderKitBean renderKit = configBean.getRenderKit(renderKitId);
        if (renderKit == null) {
            throw new IllegalArgumentException("No RenderKit for id '" + renderKitId + '\'');
        }

        RendererBean[] renderers = renderKit.getRenderers();
        if (renderers == null) {
            throw new IllegalStateException("No Renderers for RenderKit id" + '"' + renderKitId + '"');
        }

        TreeMap<String, ArrayList<RendererBean>> result = new TreeMap<String, ArrayList<RendererBean>>();

        for (int i = 0, len = renderers.length; i < len; i++) {
            RendererBean renderer = renderers[i];

            if (renderer == null) {
                throw new IllegalStateException("no Renderer");
            }

            // if this is the first time we've encountered this
            // componentFamily
            String componentFamily = renderer.getComponentFamily();
            ArrayList<RendererBean> list = result.get(componentFamily);
            if (list == null) {
                // create a list for it
                list = new ArrayList<RendererBean>();
                list.add(renderer);
                result.put(componentFamily, list);
            } else {
                list.add(renderer);
            }
        }

        return result;

    } // END getComponentFamilyRendererMap

    public static String getFirstDivFromString(String toParse, int[] out) {
        String result = null;

        if (null == toParse) {
            return result;
        }

        int divStart, divEnd;
        if (-1 != (divStart = toParse.indexOf("<div"))) {
            if (-1 != (divEnd = toParse.indexOf(">", divStart))) {
                result = toParse.substring(divStart, divEnd + 1);
            }
        }
        if (null != out && 0 < out.length) {
            out[0] = divStart;
        }

        return result;
    }

    public static String getFirstSpanFromString(String toParse, int[] out) {
        String result = null;

        if (null == toParse) {
            return result;
        }

        int spanStart, spanEnd;
        if (-1 != (spanStart = toParse.indexOf("<span"))) {
            if (-1 != (spanEnd = toParse.indexOf(">", spanStart))) {
                result = toParse.substring(spanStart, spanEnd + 1);
            }
        }
        if (null != out && 0 < out.length) {
            out[0] = spanStart;
        }

        return result;
    }

    public static FacesConfigBean getConfigBean(String facesConfig) throws Exception {

        FacesConfigBean fcb = null;
        InputStream stream = null;
        try {
            File file = new File(facesConfig);
            stream = new BufferedInputStream(new FileInputStream(file));
            InputSource source = new InputSource(file.toURL().toString());
            source.setByteStream(stream);
            fcb = (FacesConfigBean) createDigester(true, false, true).parse(source);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    ;
                }
                stream = null;
            }
        }
        return (fcb);

    } // END getConfigBean

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Configure and return a <code>Digester</code> instance suitable for use in the
     * environment specified by our parameter flags.
     * </p>
     *
     * @param design Include rules suitable for design time use in a tool
     * @param generate Include rules suitable for generating component, renderer,
     * and tag classes
     * @param runtime Include rules suitable for runtime execution
     */
    private static Digester createDigester(boolean design, boolean generate, boolean runtime) {

        Digester digester = DigesterFactory.newInstance(true).createDigester();

        // Configure parsing rules
        digester.addRuleSet(new FacesConfigRuleSet(design, generate, runtime));

        // Configure preregistered entities

        return (digester);

    } // END createDigester

}
