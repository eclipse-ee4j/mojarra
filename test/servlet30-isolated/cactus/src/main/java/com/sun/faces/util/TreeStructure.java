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

package com.sun.faces.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;

/**
 * TreeStructure is a class that represents the structure of a UIComponent
 * instance. This class plays a key role in saving and restoring the structure
 * of the component tree.
 */
public class TreeStructure implements java.io.Serializable {

    private static final long serialVersionUID = 8320767450484935667L;

    ArrayList<TreeStructure> children = null;
    HashMap<String,TreeStructure> facets = null;
    String className = null;
    String id = null;


    public TreeStructure() {
    }


    public TreeStructure(UIComponent component) {
        Util.notNull("component", component);
        this.id = component.getId();
        className = component.getClass().getName();
    }


    /**
     * Returns the className of the UIComponent that this TreeStructure
     * represents.
     */
    public String getClazzName() {
        return className;
    }


    /**
     * Returns the iterator over className of the children that are attached to
     * the UIComponent that this TreeStructure represents.
     */
    public Iterator getChildren() {
        if (children != null) {
            return (children.iterator());
        } else {
            return (Collections.EMPTY_LIST.iterator());
        }
    }


    /**
     * Returns the iterator over className of the facets that are attached to
     * the UIComponent that this TreeStructure represents.
     */
    public Iterator getFacetNames() {
        if (facets != null) {
            return (facets.keySet().iterator());
        } else {
            return (Collections.EMPTY_LIST.iterator());
        }
    }


    /**
     * Adds treeStruct as a child of this TreeStructure instance.
     */
    public void addChild(TreeStructure treeStruct) {
        Util.notNull("treeStruct", treeStruct);
        if (children == null) {
            children = new ArrayList<TreeStructure>();
        }
        children.add(treeStruct);
    }


    /**
     * Adds treeStruct as a facet belonging to this TreeStructure instance.
     */
    public void addFacet(String facetName, TreeStructure treeStruct) {
        Util.notNull("facetName", facetName);
        Util.notNull("treeStruct", treeStruct);
        if (facets == null) {
            facets = new HashMap<String, TreeStructure>();
        }
        facets.put(facetName, treeStruct);
    }


    /**
     * Returns a TreeStructure representing a facetName by looking up
     * the facet list
     */
    public TreeStructure getTreeStructureForFacet(String facetName) {
        Util.notNull("facetName", facetName);
        if (facets != null) {
            return ((facets.get(facetName)));
        } else {
            return null;
        }
    }


    /**
     * Creates and returns the UIComponent that this TreeStructure
     * represents using the structure information available.
     */
    public UIComponent createComponent() {
        UIComponent component = null;
        // create the UIComponent based on the className stored.
        try {
            Class clazz = Util.loadClass(className, this);
            component = ((UIComponent) clazz.newInstance());
        } catch (Exception e) {
            Object params[] = {className};
            throw new FacesException(MessageUtils.getExceptionMessageString(
                MessageUtils.MISSING_CLASS_ERROR_MESSAGE_ID,
                params));
        }
        assert (component != null);
        component.setId(id);
        return component;
    }
}
