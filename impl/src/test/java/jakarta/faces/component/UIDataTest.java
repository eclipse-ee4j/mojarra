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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class UIDataTest {

    /**
     * Test partial state saving.
     */
    @Test
    public void testSaveState() {
        FacesContext context = mock(FacesContext.class);
        UIData data = new UIData();
        data.markInitialState();
        assertNull(data.saveState(context));
    }

    /**
     * Test full state saving.
     */
    @Test
    public void testSaveState2() {
        FacesContext context = mock(FacesContext.class);
        UIData data = new UIData();
        assertNotNull(data.saveState(context));
    }

    /**
     * Test partial state saving with rowIndex.
     */
    @Test
    public void testSaveState3() {
        FacesContext context = mock(FacesContext.class);
        UIData data = new UIData();
        data.markInitialState();
        data.setRowIndex(4);
        assertNotNull(data.saveState(context));
    }

    @Test
    public void testInvokeOnComponentMustNotCallSetRowIndexIfNotTouched() throws Exception {
        FacesContext context = mock(FacesContext.class);
        ExternalContext externalContext = mock(ExternalContext.class);
        when(context.getExternalContext()).thenReturn(externalContext);
        RenderKit renderKit = mock(RenderKit.class);
        when(context.getRenderKit()).thenReturn(renderKit);
        context.getAttributes().put(SEPARATOR_CHAR_PARAM_NAME, SEPARATOR_CHAR);

        UIData data = new UIData() {
            @Override
            public void setRowIndex(int rowIndex) {
                context.getAttributes().put("setRowIndexCalled", true);
            }
        };

        data.setId("data");

        data.invokeOnComponent(context, "differentId", (contextInLambda, target) -> {
        });

        assertNull(context.getAttributes().get("setRowIndexCalled"));
    }
}
