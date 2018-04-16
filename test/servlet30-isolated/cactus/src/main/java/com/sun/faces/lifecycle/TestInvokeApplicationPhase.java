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

// TestInvokeApplicationPhase.java

package com.sun.faces.lifecycle;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.util.Util;
import org.apache.cactus.WebRequest;

import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import java.util.Locale;

/**
 * <B>TestInvokeApplicationPhase</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestInvokeApplicationPhase extends ServletFacesTestCase {

//
// Protected Constants
//

    public static final String DID_COMMAND = "didCommand";
    public static final String DID_FORM = "didForm";

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

    public TestInvokeApplicationPhase() {
        super("TestInvokeApplicationPhase");
    }


    public TestInvokeApplicationPhase(String name) {
        super(name);
    }

//
// Class methods
//

//
// General Methods
//

    public void testInvokeNormal() {
    }


    public void testInvokeNoOp() {
        UIInput root = new UIInput();
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        page.setViewId("default.xul");
        page.setLocale(Locale.US);
        Phase invokeApplicationPhase = new InvokeApplicationPhase();
        getFacesContext().setViewRoot(page);

        invokeApplicationPhase.execute(getFacesContext());
        assertTrue(!(getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));
    }

} // end of class TestInvokeApplicationPhase
