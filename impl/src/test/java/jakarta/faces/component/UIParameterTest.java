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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

public class UIParameterTest {

    /**
     * Test isDisable method.
     */
    @Test
    public void testIsDisable() throws Exception {
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        Method method = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        method.setAccessible(true);
        method.invoke(null, facesContext);
        UIParameter parameter = new UIParameter();
        parameter.setDisable(true);
        assertTrue(parameter.isDisable());
        method.invoke(null, (FacesContext) null);
    }

    /**
     * Test isDisable method.
     */
    @Test
    public void testIsDisable2() throws Exception {
        ELContext elContext = Mockito.mock(ELContext.class);
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        ValueExpression valueExpression = Mockito.mock(ValueExpression.class);
        Method method = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        method.setAccessible(true);
        method.invoke(null, facesContext);
        when(facesContext.getExternalContext()).thenReturn(null);
        when(valueExpression.isLiteralText()).thenReturn(false);
        when(facesContext.getELContext()).thenReturn(elContext);
        when(valueExpression.getValue(elContext)).thenReturn(true);
        UIParameter parameter = new UIParameter();
        parameter.setValueExpression("disable", valueExpression);
        assertTrue(parameter.isDisable());
        method.invoke(null, (FacesContext) null);
    }
}
