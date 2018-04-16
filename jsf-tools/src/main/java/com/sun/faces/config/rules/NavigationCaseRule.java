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

import com.sun.faces.config.beans.NavigationCaseBean;
import com.sun.faces.config.beans.NavigationRuleBean;


/**
 * <p>Digester rule for the <code>&lt;navigation-case&gt;</code> element.
 * No merges occur at this level, because there is no primary key available.</p>
 */

public class NavigationCaseRule extends FeatureRule {


    private static final String CLASS_NAME =
        "com.sun.faces.config.beans.NavigationCaseBean";


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Create an empty instance of <code>NavigationCaseBean</code>
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
     *  of type NavigationRuleBean
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {

        assert digester.peek() instanceof NavigationRuleBean
              : "Assertion Error: Expected NavigationRuleBean to be at the top of the stack";
        
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[NavigationCaseRule]{" +
                                       digester.getMatch() +
                                       "} Push " + CLASS_NAME);
        }
        Class clazz =
            digester.getClassLoader().loadClass(CLASS_NAME);
        NavigationCaseBean ncb = (NavigationCaseBean) clazz.newInstance();
        digester.push(ncb);

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
     * <p>Pop the <code>NavigationCaseBean</code> off the top of the stack,
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

        NavigationCaseBean ncb = null;
        try {
            ncb = (NavigationCaseBean) digester.pop();
        } catch (Exception e) {
            throw new IllegalStateException("Popped object is not a " +
                                            CLASS_NAME + " instance");
        }
        NavigationRuleBean nrb = (NavigationRuleBean) digester.peek();
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[NavigationCaseRule]{" +
                                       digester.getMatch() +
                                       "} New Case");
        }
        nrb.addNavigationCase(ncb);

    }


    /**
     * <p>No finish processing is required.</p>
     *
     */
    public void finish() throws Exception {
    }


    // ---------------------------------------------------------- Public Methods


    public String toString() {

        StringBuffer sb = new StringBuffer("NavigationCaseRule[className=");
        sb.append(CLASS_NAME);
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


}
