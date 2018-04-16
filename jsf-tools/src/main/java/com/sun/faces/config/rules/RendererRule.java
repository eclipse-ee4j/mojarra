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

package com.sun.faces.config.rules;


import org.xml.sax.Attributes;

import com.sun.faces.config.beans.RenderKitBean;
import com.sun.faces.config.beans.RendererBean;


/**
 * <p>Digester rule for the <code>&lt;renderer&gt;</code> element.</p>
 */

public class RendererRule extends FeatureRule {


    private static final String CLASS_NAME =
        "com.sun.faces.config.beans.RendererBean";


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Create an empty instance of <code>RendererBean</code>
     * and push it on to the object stack.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param attributes The attribute list of this element
     *
     * @exception IllegalStateException if the parent stack element is not
     *  of type FacesConfigBean
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {
        
        assert digester.peek() instanceof RenderKitBean
              : "Assertion Error: Expected RenderKitBean to be at the top of the stack";
        
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[RendererRule]{" +
                                       digester.getMatch() +
                                       "} Push " + CLASS_NAME);
        }
        Class clazz =
            digester.getClassLoader().loadClass(CLASS_NAME);
        RendererBean cb = (RendererBean) clazz.newInstance();
        digester.push(cb);

    }


    /**
     * <p>No body processing is required.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param text The text of the body of this element
     */
    public void body(String namespace, String name,
                     String text) throws Exception {
    }


    /**
     * <p>Pop the <code>RendererBean</code> off the top of the stack,
     * and either add or merge it with previous information.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     *
     * @exception IllegalStateException if the popped object is not
     *  of the correct type
     */
    public void end(String namespace, String name) throws Exception {

        RendererBean top = null;
        try {
            top = (RendererBean) digester.pop();
        } catch (Exception e) {
            throw new IllegalStateException("Popped object is not a " +
                                            CLASS_NAME + " instance");
        }
        RenderKitBean rkb = (RenderKitBean) digester.peek();
        RendererBean old = rkb.getRenderer(top.getComponentFamily(),
                                           top.getRendererType());
        if (old == null) {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[RendererRule]{" +
                                           digester.getMatch() +
                                           "} New(" +
                                           top.getComponentFamily() +
                                           "," +
                                           top.getRendererType() +
                                           ")");
            }
            rkb.addRenderer(top);
        } else {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[RendererRule]{" +
                                          digester.getMatch() +
                                          "} Merge(" +
                                          top.getComponentFamily() +
                                          "," +
                                          top.getRendererType() +
                                          ")");
            }
            mergeRenderer(top, old);
        }

    }


    /**
     * <p>No finish processing is required.</p>
     *
     */
    public void finish() throws Exception {
    }


    // ---------------------------------------------------------- Public Methods


    public String toString() {

        StringBuffer sb = new StringBuffer("RendererRule[className=");
        sb.append(CLASS_NAME);
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


    // Merge "top" into "old"
    static void mergeRenderer(RendererBean top, RendererBean old) {

        // Merge singleton properties
        if (top.getRendererClass() != null) {
            old.setRendererClass(top.getRendererClass());
        }
        if (top.isRendersChildren()) {
            old.setRendersChildren(true);
        }
	if (top.getExcludeAttributes() != null) {
	    old.setExcludeAttributes(top.getExcludeAttributes());
	}
	if (top.getTagName() != null) {
	    old.setTagName(top.getTagName());
	}

        // Merge common collections
        AttributeRule.mergeAttributes(top, old);
        mergeFeatures(top, old);

        // Merge unique collections

    }


    // Merge "top" into "old"
    static void mergeRenderers(RenderKitBean top, RenderKitBean old) {

        RendererBean rb[] = top.getRenderers();
        for (int i = 0; i < rb.length; i++) {
            RendererBean rbo = old.getRenderer(rb[i].getComponentFamily(),
                                               rb[i].getRendererType());
            if (rbo == null) {
                old.addRenderer(rb[i]);
            } else {
                mergeRenderer(rb[i], rbo);
            }
        }

    }


}
