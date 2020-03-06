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

package com.sun.faces.facelets.tag.composite;

import com.sun.faces.facelets.tag.AbstractTagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public final class CompositeLibrary extends AbstractTagLibrary {

    public final static String Namespace = "http://java.sun.com/jsf/composite";
    public final static String XMLNSNamespace = "http://xmlns.jcp.org/jsf/composite";

    public final static CompositeLibrary Instance = new CompositeLibrary();

    public CompositeLibrary() {
        this(Namespace);
    }
    
    public CompositeLibrary(String namespace) {
        super(namespace);

        // The interface section
        this.addTagHandler("interface", InterfaceHandler.class);
        
        // Things that go insead of the interface section
        this.addTagHandler("attribute", AttributeHandler.class);
        this.addTagHandler("extension", ExtensionHandler.class);
        this.addTagHandler("editableValueHolder", EditableValueHolderAttachedObjectTargetHandler.class);
        this.addTagHandler("actionSource", ActionSource2AttachedObjectTargetHandler.class);
        this.addTagHandler("valueHolder", ValueHolderAttachedObjectTargetHandler.class);
        this.addTagHandler("clientBehavior", BehaviorHolderAttachedObjectTargetHandler.class);
        this.addTagHandler("facet", DeclareFacetHandler.class);
        
        // The implementation section
        this.addTagHandler("implementation", ImplementationHandler.class);
        
        // Things that go inside of the implementation section
        this.addTagHandler("insertChildren", InsertChildrenHandler.class);
        this.addTagHandler("insertFacet", InsertFacetHandler.class);
        this.addComponent("renderFacet", "jakarta.faces.Output",
                "jakarta.faces.CompositeFacet", RenderFacetHandler.class);
    }
}
