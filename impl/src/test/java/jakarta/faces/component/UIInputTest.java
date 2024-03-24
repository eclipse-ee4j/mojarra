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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.LengthValidator;
import jakarta.faces.validator.LongRangeValidator;
import jakarta.faces.validator.Validator;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class UIInputTest {

    @Test
    public void testSaveState() {
        FacesContext context = Mockito.mock(FacesContext.class);
        UIInput input = new UIInput();
        assertNotNull(input.saveState(context));
    }

    @Test
    public void testSaveState2() {
        UIInput input = new UIInput();
        assertThrows(NullPointerException.class, () -> input.saveState(null));
    }

    @Test
    public void testSaveState3() {
        FacesContext context = Mockito.mock(FacesContext.class);
        UIInput input = new UIInput();
        input.markInitialState();
        assertNull(input.saveState(context));
    }

    @Test
    public void testSaveState4() {
        FacesContext context = Mockito.mock(FacesContext.class);
        UIInput input = new UIInput();
        LengthValidator l1 = new LengthValidator();
        LengthValidator l2 = new LengthValidator();
        input.addValidator(l1);
        input.addValidator(l2);
        l1.setMinimum(1);
        l2.setMinimum(2);
        input.markInitialState();
        assertTrue(input.initialStateMarked());
        assertTrue(l1.initialStateMarked());
        assertTrue(l2.initialStateMarked());
        Object state = input.saveState(context);
        assertNull(state);
    }

    @Test
    public void testRestoreState() {
        FacesContext context = Mockito.mock(FacesContext.class);
        UIInput input = new UIInput();
        input.restoreState(context, null);
    }

    @Test
    public void testRestoreState2() {
        FacesContext context = Mockito.mock(FacesContext.class);
        UIInput input = new UIInput();
        assertThrows(NullPointerException.class, () -> input.restoreState(null, null));
    }

    @Test
    public void testRestoreState3() {
        FacesContext context = Mockito.mock(FacesContext.class);
        UIInput input = new UIInput();
        Object state = input.saveState(context);
        assertNotNull(state);
        input.restoreState(context, state);
    }

    @Test
    public void testRestoreState4() {
        FacesContext context = Mockito.mock(FacesContext.class);
        UIInput input = new UIInput();
        input.addValidator(new LongRangeValidator());
        Object state = input.saveState(context);
        assertNotNull(state);
        input = new UIInput();
        input.restoreState(context, state);
    }

    @Test
    public void testRestoreState5() {
        FacesContext context = Mockito.mock(FacesContext.class);
        UIInput input = new UIInput();
        LengthValidator l1 = new LengthValidator();
        LengthValidator l2 = new LengthValidator();
        input.addValidator(l1);
        input.addValidator(l2);
        l1.setMinimum(1);
        l2.setMinimum(2);
        input.markInitialState();
        l2.setMinimum(3);
        assertTrue(input.initialStateMarked());
        assertTrue(l1.initialStateMarked());
        assertTrue(!l2.initialStateMarked());
        Object state = input.saveState(context);
        assertTrue(state instanceof Object[]);
        Object[] validatorState = (Object[]) ((Object[]) state)[1];
        assertNotNull(validatorState);
        assertNull(validatorState[0]);
        assertNotNull(validatorState[1]);
        assertTrue(!(validatorState[1] instanceof StateHolderSaver));
        input = new UIInput();
        l1 = new LengthValidator();
        l2 = new LengthValidator();
        l1.setMinimum(1);
        l2.setMinimum(2);
        input.addValidator(l1);
        input.addValidator(l2);
        input.restoreState(context, state);
        assertTrue(l1.getMinimum() == 1);
        assertTrue(l2.getMinimum() == 3);
        assertTrue(input.getValidators().length == 2);

        input = new UIInput();
        l1 = new LengthValidator();
        l2 = new LengthValidator();
        input.addValidator(l1);
        input.addValidator(l2);
        l1.setMinimum(1);
        l2.setMinimum(2);
        input.markInitialState();
        LengthValidator l3 = new LengthValidator();
        l3.setMinimum(3);
        input.addValidator(l3);
        state = input.saveState(context);
        assertNotNull(validatorState);
        assertTrue(state instanceof Object[]);
        validatorState = (Object[]) ((Object[]) state)[1];
        assertNotNull(validatorState);
        assertTrue(validatorState.length == 3);
        assertNotNull(validatorState[0]);
        assertNotNull(validatorState[1]);
        assertNotNull(validatorState[2]);
        assertTrue(validatorState[0] instanceof StateHolderSaver);
        assertTrue(validatorState[1] instanceof StateHolderSaver);
        assertTrue(validatorState[2] instanceof StateHolderSaver);

        input = new UIInput();
        l1 = new LengthValidator();
        l2 = new LengthValidator();
        l3 = new LengthValidator();
        LengthValidator l4 = new LengthValidator();
        input.addValidator(l1);
        input.addValidator(l2);
        input.addValidator(l3);
        input.addValidator(l4);
        l1.setMinimum(100);
        l2.setMinimum(101);
        l3.setMinimum(102);
        l4.setMinimum(103);
        assertTrue(input.getValidators().length == 4);
        input.markInitialState();
        input.restoreState(context, state);
        assertTrue(input.getValidators().length == 3);

        Validator[] validators = input.getValidators();
        for (int i = 0, len = validators.length; i < len; i++) {
            LengthValidator v = (LengthValidator) validators[i];
            assertTrue(v.getMinimum() == i + 1);
        }

    }
}
