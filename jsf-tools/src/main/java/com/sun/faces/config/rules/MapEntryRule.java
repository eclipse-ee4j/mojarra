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

import com.sun.faces.config.beans.MapEntriesBean;
import com.sun.faces.config.beans.MapEntryBean;
import org.apache.commons.digester.Rule;


/**
 * <p>Digester rule for the <code>&lt;map-entry&gt;</code> element.</p>
 */

public class MapEntryRule extends Rule {


    private static final String CLASS_NAME =
        "com.sun.faces.config.beans.MapEntryBean";


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Create an empty instance of <code>MapEntryBean</code>
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
     *  of type MapEntryHolder
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {

        assert digester.peek() instanceof MapEntriesBean
              : "Assertion Error: Expected MapEntriesBean to be at the top of the stack";
        
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[MapEntryRule]{" +
                                       digester.getMatch() +
                                       "} Push " + CLASS_NAME);
        }
        Class clazz =
            digester.getClassLoader().loadClass(CLASS_NAME);
        MapEntryBean meb = (MapEntryBean) clazz.newInstance();
        digester.push(meb);

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
     * <p>Pop the <code>MapEntryBean</code> off the top of the stack,
     * and add the new entry.</p>
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

        MapEntryBean top = null;
        try {
            top = (MapEntryBean) digester.pop();
        } catch (Exception e) {
            throw new IllegalStateException("Popped object is not a " +
                                            CLASS_NAME + " instance");
        }
        MapEntriesBean mesb = (MapEntriesBean) digester.peek();
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[MapEntryRule]{" +
                                       digester.getMatch() +
                                       "} Add");
        }
        mesb.addMapEntry(top);

    }


    /**
     * <p>No finish processing is required.</p>
     *
     */
    public void finish() throws Exception {
    }


    // ---------------------------------------------------------- Public Methods


    public String toString() {

        StringBuffer sb = new StringBuffer("MapEntryRule[className=");
        sb.append(CLASS_NAME);
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


}
