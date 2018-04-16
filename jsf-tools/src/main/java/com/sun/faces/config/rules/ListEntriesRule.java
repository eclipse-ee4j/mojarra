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

import com.sun.faces.config.beans.ListEntriesBean;
import com.sun.faces.config.beans.ListEntriesHolder;
import org.apache.commons.digester.Rule;


/**
 * <p>Digester rule for the <code>&lt;list-entries&gt;</code> element.</p>
 */

public class ListEntriesRule extends Rule {


    private static final String CLASS_NAME =
        "com.sun.faces.config.beans.ListEntriesBean";


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Create an empty instance of <code>ListEntriesBean</code>
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
     *  of type ListEntriesHolder
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {

        assert digester.peek() instanceof ListEntriesHolder
              : "Assertion Error: Expected ListEntriesHolder to be at the top of the stack";
      
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[ListEntriesRule]{" +
                                       digester.getMatch() +
                                       "} Push " + CLASS_NAME);
        }
        Class clazz =
            digester.getClassLoader().loadClass(CLASS_NAME);
        ListEntriesBean leb = (ListEntriesBean) clazz.newInstance();
        digester.push(leb);

    }


    /**
     * <p>No body processing is requlred.</p>
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
     * <p>Pop the <code>ListEntriesBean</code> off the top of the stack,
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

        ListEntriesBean top = null;
        try {
            top = (ListEntriesBean) digester.pop();
        } catch (Exception e) {
            throw new IllegalStateException("Popped object is not a " +
                                            CLASS_NAME + " instance");
        }
        ListEntriesHolder leh = (ListEntriesHolder) digester.peek();
        ListEntriesBean old = leh.getListEntries();
        if (old == null) {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[ListEntriesRule]{" +
                                           digester.getMatch() +
                                           "} New");
            }
            leh.setListEntries(top);
        } else {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[ManagedBeanRule]{" +
                                          digester.getMatch() +
                                          "} Merge");
            }
            mergeListEntries(top, old);
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

        StringBuffer sb = new StringBuffer("ListEntriesRule[className=");
        sb.append(CLASS_NAME);
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


    // Merge "top" into "old"
    static void mergeListEntries(ListEntriesBean top, ListEntriesBean old) {

        // Merge singleton properties
        if (top.getValueClass() != null) {
            old.setValueClass(top.getValueClass());
        }

        // Merge common collections

        // Merge unique collections
        String values[] = top.getValues();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                old.addNullValue();
            } else {
                old.addValue(values[i]);
            }
        }

    }


    // Merge "top" into "old"
    static void mergeListEntries(ListEntriesHolder top, ListEntriesHolder old) {

        ListEntriesBean lebt = top.getListEntries();
        if (lebt != null) {
            ListEntriesBean lebo = old.getListEntries();
            if (lebo != null) {
                mergeListEntries(lebt, lebo);
            } else {
                old.setListEntries(lebt);
            }
        }

    }

}
