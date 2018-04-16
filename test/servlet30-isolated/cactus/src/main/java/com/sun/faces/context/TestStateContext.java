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

// TestStateContext.java

package com.sun.faces.context;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.util.ComponentStruct;
import com.sun.faces.util.Util;
import java.util.List;
import java.util.Locale;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * <B>TestStateContext</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestStateContext extends ServletFacesTestCase {

//
// Protected Constants
//

//
// Class Variables
//

//
// Instance Variables
//

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public TestStateContext() {
        super("TestStateContext");
    }


    public TestStateContext(String name) {
        super(name);
    }
//
// Class methods
//

//
// Methods from TestCase
//
    @Override
    public void setUp() {
        super.setUp();
        UIViewRoot viewRoot = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        viewRoot.setViewId("viewId");
        viewRoot.setLocale(Locale.US);
        getFacesContext().setViewRoot(viewRoot);
    }

//
// General Methods
//

    public void testGetStateContext() {
        StateContext stateContext = StateContext.getStateContext(getFacesContext());
        assertTrue(null != stateContext);
    }

    public void testPartialStateSaving() {
    	  FacesContext ctx = getFacesContext();
        StateContext stateContext = StateContext.getStateContext(ctx);
        boolean partial = stateContext.isPartialStateSaving(ctx, "10");
        assertTrue(partial);
    }

    public void testAddComponent() {
        FacesContext ctx = getFacesContext();
        StateContext stateContext = StateContext.getStateContext(ctx);
        UIViewRoot viewRoot = ctx.getViewRoot();
        assertTrue(viewRoot != null);
        stateContext.startTrackViewModifications(ctx, viewRoot);
        UIOutput output = new UIOutput();
        output.setId("foo");
        viewRoot.getChildren().add(output);
        List<ComponentStruct> added = stateContext.getDynamicActions();
        assertTrue(added.size() > 0);
    }
}
