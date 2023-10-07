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

package com.sun.faces.facelets.tag.composite;

import static com.sun.faces.util.Util.unmodifiableSet;

import java.util.Set;

import com.sun.faces.facelets.tag.AbstractTagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public final class CompositeLibrary extends AbstractTagLibrary {

    private final static String SunNamespace = "http://java.sun.com/jsf/composite";
    private final static String JcpNamespace = "http://xmlns.jcp.org/jsf/composite";
    private final static String JakartaNamespace = "jakarta.faces.composite";

    public final static Set<String> NAMESPACES = unmodifiableSet(JakartaNamespace, JcpNamespace, SunNamespace);
    public final static String DEFAULT_NAMESPACE = JakartaNamespace;

    public CompositeLibrary(String namespace) {
        super(namespace);

        // The interface section
        addTagHandler("interface", InterfaceHandler.class);

        // Things that go insead of the interface section
        addTagHandler("attribute", AttributeHandler.class);
        addTagHandler("extension", ExtensionHandler.class);
        addTagHandler("editableValueHolder", EditableValueHolderAttachedObjectTargetHandler.class);
        addTagHandler("actionSource", ActionSourceAttachedObjectTargetHandler.class);
        addTagHandler("valueHolder", ValueHolderAttachedObjectTargetHandler.class);
        addTagHandler("clientBehavior", BehaviorHolderAttachedObjectTargetHandler.class);
        addTagHandler("facet", DeclareFacetHandler.class);

        // The implementation section
        addTagHandler("implementation", ImplementationHandler.class);

        // Things that go inside of the implementation section
        addTagHandler("insertChildren", InsertChildrenHandler.class);
        addTagHandler("insertFacet", InsertFacetHandler.class);
        this.addComponent("renderFacet", "jakarta.faces.Output", "jakarta.faces.CompositeFacet", RenderFacetHandler.class);
    }
}
