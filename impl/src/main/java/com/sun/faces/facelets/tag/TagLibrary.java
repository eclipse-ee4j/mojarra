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

package com.sun.faces.facelets.tag;

import java.lang.reflect.Method;

import jakarta.faces.FacesException;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

/**
 * A library of Tags associated with one or more namespaces.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public interface TagLibrary {

    /**
     * true if the namespace is used in this library
     *
     * @param ns namespace
     * @param t the tag instance currently active at the time this method is called. May be null
     *
     */
    boolean containsNamespace(String ns, Tag t);

    /**
     * If this library contains a TagHandler for the namespace and local name true if handled by this library
     *
     * @param ns namespace
     * @param localName local name
     */
    boolean containsTagHandler(String ns, String localName);

    /**
     * Create a new instance of a TagHandler, using the passed TagConfig
     *
     * @param ns namespace
     * @param localName local name
     * @param tag configuration information
     * @return a new TagHandler instance
     * @throws FacesException
     */
    TagHandler createTagHandler(String ns, String localName, TagConfig tag) throws FacesException;

    /**
     * If this library contains the specified function name
     *
     * @param ns namespace
     * @param name function name
     * @return true if handled
     */
    boolean containsFunction(String ns, String name);

    /**
     * Return a Method instance for the passed namespace and name
     *
     * @param ns namespace
     * @param name function name
     */
    Method createFunction(String ns, String name);
}
