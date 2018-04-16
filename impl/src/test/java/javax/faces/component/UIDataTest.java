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

package javax.faces.component;

import javax.faces.context.FacesContext;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class UIDataTest {
    
    /**
     * Test partial state saving.
     */
    @Test
    public void testSaveState() {
        FacesContext context = EasyMock.createMock(FacesContext.class);
        UIData data = new UIData();
        data.markInitialState();
        replay(context);
        assertNull(data.saveState(context));
        verify(context);
    }
    
    /**
     * Test full state saving.
     */
    @Test
    public void testSaveState2() {
        FacesContext context = EasyMock.createMock(FacesContext.class);
        UIData data = new UIData();
        replay(context);
        assertNotNull(data.saveState(context));
        verify(context);
    }
    
    /**
     * Test partial state saving with rowIndex.
     */
    @Test
    public void testSaveState3() {
        FacesContext context = EasyMock.createMock(FacesContext.class);
        UIData data = new UIData();
        data.markInitialState();
        data.setRowIndex(4);
        replay(context);
        assertNotNull(data.saveState(context));
        verify(context);
    }
}
