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

package jakarta.faces.component;

import jakarta.faces.convert.IntegerConverter;
import junit.framework.Test;
import junit.framework.TestSuite;

public class StateHolderSaverTestCase extends UIComponentBaseTestCase {

    // ------------------------------------------------------ Instance Variables
    // ------------------------------------------------------------ Constructors
    // Construct a new instance of this test case.
    public StateHolderSaverTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Return the tests included in this test case.
    public static Test suite() {
        return new TestSuite(StateHolderSaverTestCase.class);
    }

    // ------------------------------------------------- Individual Test Methods
    @Override
    public void testLifecycleManagement() {
    }

    @Override
    public void testChildrenRecursive() {
    }

    @Override
    public void testComponentReconnect() {
    }

    @Override
    public void testComponentRemoval() {
    }

    public void testImplementsStateHolder() throws Exception {
        StateHolderSaver saver = null;
        UIInput postSave, preSave = new UIInput();
        preSave.setId("id1");
        preSave.setRendererType(null);

        saver = new StateHolderSaver(facesContext, preSave);
        postSave = (UIInput) saver.restore(facesContext);
        assertEquals(postSave.getId(), preSave.getId());
    }

    public void testImplementsSerializable() throws Exception {
        StateHolderSaver saver = null;
        String preSave = "hello";
        String postSave = null;

        saver = new StateHolderSaver(facesContext, preSave);
        postSave = (String) saver.restore(facesContext);
        assertTrue(preSave.equals(postSave));
    }

    public void testImplementsNeither() throws Exception {
        StateHolderSaver saver = null;
        IntegerConverter preSave = new IntegerConverter(), postSave = null;

        saver = new StateHolderSaver(facesContext, preSave);
        postSave = (IntegerConverter) saver.restore(facesContext);
        assertTrue(postSave != null); // lack of ClassCastException
    }
}
