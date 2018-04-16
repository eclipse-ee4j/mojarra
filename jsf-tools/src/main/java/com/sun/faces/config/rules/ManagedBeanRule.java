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


import java.util.Arrays;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.ManagedBeanBean;
import com.sun.faces.util.ToolsUtil;


/**
 * <p>Digester rule for the <code>&lt;managed-bean&gt;</code> element.</p>
 */

public class ManagedBeanRule extends FeatureRule {


    private static final String CLASS_NAME =
        "com.sun.faces.config.beans.ManagedBeanBean";

    private static final String[] SCOPES = {
        "none", "application", "session", "request"
    };

    static {
        Arrays.sort(SCOPES);
    }


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Create an empty instance of <code>ManagedBeanBean</code>
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
        
        assert digester.peek() instanceof FacesConfigBean
              : "Assertion Error: Expected FacesConfigBean to be at the top of the stack";
       
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[ManagedBeanRule]{" +
                                       digester.getMatch() +
                                       "} Push " + CLASS_NAME);
        }
        Class clazz =
            digester.getClassLoader().loadClass(CLASS_NAME);
        ManagedBeanBean mbb = (ManagedBeanBean) clazz.newInstance();
        digester.push(mbb);

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
     * <p>Pop the <code>ManagedBeanBean</code> off the top of the stack,
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

        ManagedBeanBean top;
        try {
            top = (ManagedBeanBean) digester.pop();
        } catch (Exception e) {
            throw new IllegalStateException("Popped object is not a " +
                                            CLASS_NAME + " instance");
        }

        validate(top);

        FacesConfigBean fcb = (FacesConfigBean) digester.peek();
        ManagedBeanBean old = fcb.getManagedBean(top.getManagedBeanName());
        if (old == null) {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[ManagedBeanRule]{" +
                                           digester.getMatch() +
                                           "} New(" +
                                           top.getManagedBeanName() +
                                           ")");
            }
            fcb.addManagedBean(top);
        } else {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[ManagedBeanRule]{" +
                                          digester.getMatch() +
                                          "} Merge(" +
                                          top.getManagedBeanName() +
                                          ")");
            }
            mergeManagedBean(top, old);
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

        StringBuffer sb = new StringBuffer("ManagedBeanRule[className=");
        sb.append(CLASS_NAME);
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


    // Merge "top" into "old"
    static void mergeManagedBean(ManagedBeanBean top, ManagedBeanBean old) {

        // Merge singleton properties
        if (top.getManagedBeanClass() != null) {
            old.setManagedBeanClass(top.getManagedBeanClass());
        }
        if (top.getManagedBeanScope() != null) {
            old.setManagedBeanScope(top.getManagedBeanScope());
        }

        // Merge common collections
        mergeFeatures(top, old);

        // Merge unique collections
        ListEntriesRule.mergeListEntries(top, old);
        ManagedPropertyRule.mergeManagedProperties(top, old);
        MapEntriesRule.mergeMapEntries(top, old);

    }


    // --------------------------------------------------------- Private Methods

    /**
     * <p>Provides simple sanity checks.</p>
     * @param bean the <code>ManagedBeanBean</code> instance to validate
     */
    private void validate(ManagedBeanBean bean) {

        String val = bean.getManagedBeanName();
        if (val == null || val.length() == 0) {
            Locator locator = digester.getDocumentLocator();
            String documentName = "UNKNOWN";
            String lineNumber = "UNKNWOWN";

            if (locator != null) {
                documentName = locator.getSystemId();
                lineNumber = Integer.toString(locator.getLineNumber());
            }

            throw new IllegalStateException(ToolsUtil.getMessage(
                ToolsUtil.MANAGED_BEAN_NO_MANAGED_BEAN_NAME_ID,
                new Object[]{documentName, lineNumber}));
        }

        val = bean.getManagedBeanClass();
        if (val == null || val.length() == 0) {
            throw new IllegalStateException(ToolsUtil.getMessage(
                ToolsUtil.MANAGED_BEAN_NO_MANAGED_BEAN_CLASS_ID,
                new Object[]{ bean.getManagedBeanName() }));
        }

        val = bean.getManagedBeanScope();
        if (val == null || val.length() == 0) {
            throw new IllegalStateException(ToolsUtil.getMessage(
                ToolsUtil.MANAGED_BEAN_NO_MANAGED_BEAN_SCOPE_ID,
                new Object[]{ bean.getManagedBeanName() }));
        }

        if (Arrays.binarySearch(SCOPES, val) < 0) {
            throw new IllegalStateException(ToolsUtil.getMessage(
                ToolsUtil.MANAGED_BEAN_INVALID_SCOPE_ID,
                new Object[]{ val, bean.getManagedBeanName() }));
        }

        // - if the managed bean is itself a List, make sure it has no
        //   map entries or managed properties
        // - if the managed bean is itself a Map, make sure it has no
        //   managed properties
        if (bean.getListEntries() != null) {
            if (bean.getMapEntries() != null ||
                bean.getManagedProperties().length != 0) {
                throw new IllegalStateException (
                    ToolsUtil.getMessage(
                        ToolsUtil.MANAGED_BEAN_AS_LIST_CONFIG_ERROR_ID,
                        new Object[]{ bean.getManagedBeanName() }));
            }
        } else if (bean.getMapEntries() != null) {
            if (bean.getManagedProperties().length != 0) {
                throw new IllegalStateException (
                    ToolsUtil.getMessage(
                        ToolsUtil.MANAGED_BEAN_AS_MAP_CONFIG_ERROR_ID,
                        new Object[]{ bean.getManagedBeanName() }));
            }
        }

    } // END validate



}
