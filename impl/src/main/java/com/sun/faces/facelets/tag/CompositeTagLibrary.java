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
import java.util.List;

import com.sun.faces.facelets.compiler.CompilationMessageHolder;
import com.sun.faces.facelets.tag.faces.CompositeComponentTagLibrary;
import com.sun.faces.facelets.tag.faces.FacesComponentTagLibrary;
import com.sun.faces.facelets.tag.faces.LazyTagLibrary;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

/**
 * A TagLibrary that is composed of 1 or more TagLibrary children. Uses the chain of responsibility pattern to stop
 * searching as soon as one of the children handles the requested method.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class CompositeTagLibrary implements TagLibrary {

    private TagLibrary[] libraries;
    private CompilationMessageHolder messageHolder;

    public CompositeTagLibrary(TagLibrary[] libraries, CompilationMessageHolder unit) {
        Util.notNull("libraries", libraries);
        this.libraries = libraries;
        messageHolder = unit;
    }

    public CompositeTagLibrary(TagLibrary[] libraries) {
        this(libraries, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.tag.TagLibrary#containsNamespace(java.lang.String)
     */
    @Override
    public boolean containsNamespace(String ns, Tag t) {
        for (int i = 0; i < libraries.length; i++) {
            if (libraries[i].containsNamespace(ns, null)) {
                return true;
            }
        }
        // PENDING: this is a terribly inefficient impl. Needs refactoring.
        LazyTagLibrary lazyLibraries[] = new LazyTagLibrary[2];
        lazyLibraries[0] = new CompositeComponentTagLibrary(ns);
        lazyLibraries[1] = new FacesComponentTagLibrary(ns);
        LazyTagLibrary toTest = null;
        for (int i = 0; i < lazyLibraries.length; i++) {
            if (lazyLibraries[i].tagLibraryForNSExists(ns)) {
                toTest = lazyLibraries[i];
                break;
            }
        }
        if (null != toTest) {
            TagLibrary[] librariesPlusOne = new TagLibrary[libraries.length + 1];
            System.arraycopy(libraries, 0, librariesPlusOne, 0, libraries.length);
            librariesPlusOne[libraries.length] = toTest;
            for (int i = 0; i < libraries.length; i++) {
                libraries[i] = null;
            }
            libraries = librariesPlusOne;
            return true;
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context.isProjectStage(ProjectStage.Development)) {
                if (null != t && !ns.equals("http://www.w3.org/1999/xhtml")) {
                    // messageHolder will only be null in the case of the private
                    // EMPTY_LIBRARY class variable of the Compiler class.
                    // This code will never be called on that CompositeTagLibrary
                    // instance.
                    assert null != messageHolder;
                    String prefix = getPrefixFromTag(t);
                    if (null != prefix) {
                        List<FacesMessage> prefixMessages = messageHolder.getNamespacePrefixMessages(context, prefix);
                        prefixMessages.add(new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning: This page calls for XML namespace " + ns
                                + " declared with prefix " + prefix + " but no taglibrary exists for that namespace.", ""));
                    }
                }
            }
        }
        return false;
    }

    private String getPrefixFromTag(Tag t) {
        String result = t.getQName();
        if (null != result) {
            int i;
            if (-1 != (i = result.indexOf(":"))) {
                return result.substring(0, i);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.tag.TagLibrary#containsTagHandler(java.lang.String, java.lang.String)
     */
    @Override
    public boolean containsTagHandler(String ns, String localName) {
        for (int i = 0; i < libraries.length; i++) {
            if (libraries[i].containsTagHandler(ns, localName)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.tag.TagLibrary#createTagHandler(java.lang.String, java.lang.String,
     * com.sun.facelets.tag.TagConfig)
     */
    @Override
    public TagHandler createTagHandler(String ns, String localName, TagConfig tag) throws FacesException {
        for (int i = 0; i < libraries.length; i++) {
            if (libraries[i].containsTagHandler(ns, localName)) {
                return libraries[i].createTagHandler(ns, localName, tag);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.tag.TagLibrary#containsFunction(java.lang.String, java.lang.String)
     */
    @Override
    public boolean containsFunction(String ns, String name) {
        for (int i = 0; i < libraries.length; i++) {
            if (libraries[i].containsFunction(ns, name)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.tag.TagLibrary#createFunction(java.lang.String, java.lang.String)
     */
    @Override
    public Method createFunction(String ns, String name) {
        for (int i = 0; i < libraries.length; i++) {
            if (libraries[i].containsFunction(ns, name)) {
                return libraries[i].createFunction(ns, name);
            }
        }
        return null;
    }
}
