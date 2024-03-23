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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jakarta.faces.convert.IntegerConverter;

public class StateHolderSaverTestCase extends UIComponentBaseTestCase {

    // ------------------------------------------------- Individual Test Methods
    @Override
    @Test
    public void testLifecycleManagement() {
    }

    @Override
    @Test
    public void testChildrenRecursive() {
    }

    @Override
    @Test
    public void testComponentReconnect() {
    }

    @Override
    @Test
    public void testComponentRemoval() {
    }

    @Test
    public void testImplementsStateHolder() throws Exception {
        StateHolderSaver saver = null;
        UIInput postSave, preSave = new UIInput();
        preSave.setId("id1");
        preSave.setRendererType(null);

        saver = new StateHolderSaver(facesContext, preSave);
        postSave = (UIInput) saver.restore(facesContext);
        assertEquals(postSave.getId(), preSave.getId());
    }

    @Test
    public void testImplementsSerializable() throws Exception {
        StateHolderSaver saver = null;
        String preSave = "hello";
        String postSave = null;

        saver = new StateHolderSaver(facesContext, preSave);
        postSave = (String) saver.restore(facesContext);
        assertTrue(preSave.equals(postSave));
    }

    @Test
    public void testImplementsNeither() throws Exception {
        StateHolderSaver saver = null;
        IntegerConverter preSave = new IntegerConverter(), postSave = null;

        saver = new StateHolderSaver(facesContext, preSave);
        postSave = (IntegerConverter) saver.restore(facesContext);
        assertTrue(postSave != null); // lack of ClassCastException
    }
}
