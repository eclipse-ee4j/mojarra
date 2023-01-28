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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

public class UIParameterTest {

    /**
     * Test isDisable method.
     */
    @Test
    public void testIsDisable() throws Exception {
        FacesContext facesContext = PowerMock.createNicePartialMockForAllMethodsExcept(FacesContext.class, "getCurrentInstance", "setCurrentInstance");
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
        ELContext elContext = PowerMock.createMock(ELContext.class);
        FacesContext facesContext = PowerMock.createPartialMockForAllMethodsExcept(FacesContext.class, "getCurrentInstance", "setCurrentInstance");
        ValueExpression valueExpression = PowerMock.createMock(ValueExpression.class);
        Method method = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        method.setAccessible(true);
        method.invoke(null, facesContext);
        expect(facesContext.getExternalContext()).andReturn(null).anyTimes();
        expect(valueExpression.isLiteralText()).andReturn(false).anyTimes();
        expect(facesContext.getELContext()).andReturn(elContext);
        expect(valueExpression.getValue(elContext)).andReturn(true);
        replay(elContext, facesContext, valueExpression);
        UIParameter parameter = new UIParameter();
        parameter.setValueExpression("disable", valueExpression);
        assertTrue(parameter.isDisable());
        verify(elContext, facesContext, valueExpression);
        method.invoke(null, (FacesContext) null);
    }
}
