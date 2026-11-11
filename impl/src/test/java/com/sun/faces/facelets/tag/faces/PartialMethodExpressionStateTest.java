/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.faces.facelets.tag.faces;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.mock.MockExternalContext;
import com.sun.faces.mock.MockFacesContext;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockLifecycle;
import com.sun.faces.mock.MockServletContext;

import jakarta.el.MethodExpression;
import jakarta.faces.FactoryFinder;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionListener;
import jakarta.faces.validator.Validator;

/**
 * The method expression listeners and validator which the build of the view creates for the <code>actionListener</code>,
 * <code>valueChangeListener</code> and <code>validator</code> attributes leave no state in a component whose initial
 * state is marked, and keep their full state whenever the component saves its full state.
 */
public class PartialMethodExpressionStateTest {

    private FacesContext context;

    @BeforeEach
    public void setUp() {
        MockServletContext servletContext = new MockServletContext();
        MockExternalContext externalContext = new MockExternalContext(servletContext, new MockHttpServletRequest(null), new MockHttpServletResponse());
        context = new MockFacesContext(externalContext, new MockLifecycle());
    }

    @AfterEach
    public void tearDown() throws Exception {
        FactoryFinder.releaseFactories();
        Method reInitializeFactoryManager = FactoryFinder.class.getDeclaredMethod("reInitializeFactoryManager", (Class<?>[]) null);
        reInitializeFactoryManager.setAccessible(true);
        reInitializeFactoryManager.invoke(null, (Object[]) null);
    }

    @Test
    public void testMarkedCommandSavesNoActionListenerState() {
        UICommand command = new UICommand();
        command.addActionListener(new PartialMethodExpressionActionListener(mock(MethodExpression.class), mock(MethodExpression.class)));
        command.markInitialState();

        assertNull(command.saveState(context));
    }

    @Test
    public void testMarkedInputSavesNoValueChangeListenerNorValidatorState() {
        UIInput input = new UIInput();
        input.addValueChangeListener(new PartialMethodExpressionValueChangeListener(mock(MethodExpression.class), mock(MethodExpression.class)));
        input.addValidator(new PartialMethodExpressionValidator(mock(MethodExpression.class)));
        input.markInitialState();

        assertNull(input.saveState(context));
    }

    @Test
    public void testClearedActionListenerSavesFullState() {
        MethodExpression oneArg = mock(MethodExpression.class);
        MethodExpression zeroArg = mock(MethodExpression.class);
        PartialMethodExpressionActionListener listener = new PartialMethodExpressionActionListener(oneArg, zeroArg);
        listener.markInitialState();
        listener.clearInitialState();

        assertArrayEquals(new Object[] { oneArg, zeroArg }, (Object[]) listener.saveState(context));
    }

    @Test
    public void testClearedValueChangeListenerSavesFullState() {
        MethodExpression oneArg = mock(MethodExpression.class);
        MethodExpression zeroArg = mock(MethodExpression.class);
        PartialMethodExpressionValueChangeListener listener = new PartialMethodExpressionValueChangeListener(oneArg, zeroArg);
        listener.markInitialState();
        listener.clearInitialState();

        assertArrayEquals(new Object[] { oneArg, zeroArg }, (Object[]) listener.saveState(context));
    }

    @Test
    public void testClearedValidatorSavesFullState() {
        MethodExpression expression = mock(MethodExpression.class);
        PartialMethodExpressionValidator validator = new PartialMethodExpressionValidator(expression);
        validator.markInitialState();
        validator.clearInitialState();

        assertArrayEquals(new Object[] { expression }, (Object[]) validator.saveState(context));
    }

    @Test
    public void testActionListenerAddedAfterMarkingIsRestoredFromFullState() {
        MethodExpression oneArg = mock(MethodExpression.class);
        MethodExpression zeroArg = mock(MethodExpression.class);
        UICommand command = new UICommand();
        command.markInitialState();
        command.addActionListener(new PartialMethodExpressionActionListener(oneArg, zeroArg));
        Object state = command.saveState(context);
        assertNotNull(state);

        UICommand restored = new UICommand();
        restored.restoreState(context, state);

        ActionListener[] listeners = restored.getActionListeners();
        assertEquals(1, listeners.length);
        PartialMethodExpressionActionListener listener = assertInstanceOf(PartialMethodExpressionActionListener.class, listeners[0]);
        assertArrayEquals(new Object[] { oneArg, zeroArg }, (Object[]) listener.saveState(context));
    }

    @Test
    public void testMarkedValidatorKeepsItsExpressionWhenSiblingStateIsRestored() {
        MethodExpression expression = mock(MethodExpression.class);
        Object state = buildMarkedInput(expression, "changed").saveState(context);
        assertNotNull(state);

        UIInput rebuilt = buildMarkedInput(expression, "built");
        rebuilt.restoreState(context, state);

        Validator<?>[] validators = rebuilt.getValidators();
        PartialMethodExpressionValidator validator = assertInstanceOf(PartialMethodExpressionValidator.class, validators[0]);
        validator.clearInitialState();
        assertArrayEquals(new Object[] { expression }, (Object[]) validator.saveState(context));
        assertEquals("changed", ((StatefulValidator) validators[1]).config);
    }

    private static UIInput buildMarkedInput(MethodExpression expression, String siblingConfig) {
        UIInput input = new UIInput();
        input.addValidator(new PartialMethodExpressionValidator(expression));
        StatefulValidator sibling = new StatefulValidator();
        input.addValidator(sibling);
        input.markInitialState();
        sibling.config = siblingConfig;
        return input;
    }

    /**
     * A validator implementing only {@link StateHolder}, which therefore always saves its state.
     */
    public static class StatefulValidator implements Validator<Object>, StateHolder {

        String config = "built";

        @Override
        public void validate(FacesContext context, UIComponent component, Object value) {
        }

        @Override
        public Object saveState(FacesContext context) {
            return config;
        }

        @Override
        public void restoreState(FacesContext context, Object state) {
            config = (String) state;
        }

        @Override
        public boolean isTransient() {
            return false;
        }

        @Override
        public void setTransient(boolean newTransientValue) {
        }
    }

}
