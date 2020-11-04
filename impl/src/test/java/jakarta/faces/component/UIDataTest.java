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

import static jakarta.faces.component.NamingContainer.SEPARATOR_CHAR;
import static jakarta.faces.component.UINamingContainer.SEPARATOR_CHAR_PARAM_NAME;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.createNicePartialMockAndInvokeDefaultConstructor;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import jakarta.faces.context.FacesContext;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class UIDataTest {

    /**
     * Test partial state saving.
     */
    @Test
    public void testSaveState() {
        FacesContext context = createMock(FacesContext.class);
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
        FacesContext context = createMock(FacesContext.class);
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
        FacesContext context = createMock(FacesContext.class);
        UIData data = new UIData();
        data.markInitialState();
        data.setRowIndex(4);
        replay(context);
        assertNotNull(data.saveState(context));
        verify(context);
    }

    @Test
    public void testInvokeOnComponentMustNotCallSetRowIndexIfNotTouched() throws Exception {
        FacesContext context = createNicePartialMockAndInvokeDefaultConstructor(FacesContext.class, "getRenderKit");
        context.getAttributes().put(SEPARATOR_CHAR_PARAM_NAME, SEPARATOR_CHAR);

        UIData data = new UIData() {
            @Override
            public void setRowIndex(int rowIndex) {
                context.getAttributes().put("setRowIndexCalled", true);
            }
        };

        data.setId("data");
        // simple way. otherwise, we have to mock the renderkit and whatever.
        Whitebox.setInternalState(data, "clientId", data.getId());

        data.invokeOnComponent(context, "differentId", (contextInLambda, target) -> {
        });

        assertThat(context.getAttributes().get("setRowIndexCalled"), is(nullValue()));
    }
}
