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

package com.sun.faces.facelets.compiler;

import com.sun.faces.facelets.tag.TagLibrary;
import com.sun.faces.facelets.tag.ui.UILibrary;

import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagConfig;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
class TagUnit extends CompilationUnit implements TagConfig {

    private final TagLibrary library;

    private final String id;

    private final Tag tag;

    private final String namespace;

    private final String name;

    public TagUnit(TagLibrary library, String namespace, String name, Tag tag, String id) {
        this.library = library;
        this.tag = tag;
        this.namespace = namespace;
        this.name = name;
        this.id = id;
    }

    /*
     * special case if you have a ui:composition tag. If and only if the composition is on the same facelet page as the
     * composite:implementation, throw a FacesException with a helpful error message.
     */

    @Override
    protected void startNotify(CompilationManager manager) {
        if (name.equals("composition") && UILibrary.NAMESPACES.contains(namespace)) {
            CompilerPackageCompilationMessageHolder messageHolder = (CompilerPackageCompilationMessageHolder) manager.getCompilationMessageHolder();
            CompilationManager compositeComponentCompilationManager = messageHolder.getCurrentCompositeComponentCompilationManager();
            if (manager.equals(compositeComponentCompilationManager)) {
                // PENDING I18N
                String messageStr = "Because the definition of ui:composition causes any " + "parent content to be ignored, it is invalid to use "
                        + "ui:composition directly inside of a composite component. " + "Consider ui:decorate instead.";
                throw new FaceletException(messageStr);
            }
        }
    }

    @Override
    public FaceletHandler createFaceletHandler() {
        return library.createTagHandler(namespace, name, this);
    }

    @Override
    public FaceletHandler getNextHandler() {
        return getNextFaceletHandler();
    }

    @Override
    public Tag getTag() {
        return tag;
    }

    @Override
    public String getTagId() {
        return id;
    }

    @Override
    public String toString() {
        return tag.toString();
    }

}
