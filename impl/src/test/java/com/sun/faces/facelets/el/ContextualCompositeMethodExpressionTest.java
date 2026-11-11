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

package com.sun.faces.facelets.el;

import static com.sun.faces.facelets.el.ContextualCompositeMethodExpression.invokeOuterCompositeMethodExpression;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.validator.ValidatorException;

/**
 * Tests that the method expression of an outer composite component is invoked when the source expression evaluates to
 * one, that the given {@link MethodNotFoundException} is thrown when there is no such method, so that the caller can
 * try another variant of the method, and that any other exception of evaluating the source or of the invoked method
 * propagates.
 */
class ContextualCompositeMethodExpressionTest {

    private static final Object[] PARAMS = { "event" };

    private final ELContext context = mock(ELContext.class);
    private final ValueExpression source = mock(ValueExpression.class);
    private final MethodExpression outer = mock(MethodExpression.class);
    private final MethodNotFoundException notFound = new MethodNotFoundException("inner");

    @Test
    void outerMethodExpressionIsInvoked() {
        when(source.getValue(context)).thenReturn(outer);
        when(outer.invoke(context, PARAMS)).thenReturn("result");

        assertEquals("result", invokeOuterCompositeMethodExpression(context, source, PARAMS, notFound));
    }

    @Test
    void sourceWhichCannotBeEvaluatedThrowsGivenMethodNotFoundException() {
        when(source.getValue(context)).thenThrow(new PropertyNotFoundException());

        assertSame(notFound, assertThrows(MethodNotFoundException.class, () -> invokeOuterCompositeMethodExpression(context, source, PARAMS, notFound)));
    }

    @Test
    void exceptionOfEvaluatingSourcePropagates() {
        ELException thrown = new ELException(new IllegalStateException());
        when(source.getValue(context)).thenThrow(thrown);

        assertSame(thrown, assertThrows(ELException.class, () -> invokeOuterCompositeMethodExpression(context, source, PARAMS, notFound)));
    }

    @Test
    void sourceWhichIsNoMethodExpressionThrowsGivenMethodNotFoundException() {
        when(source.getValue(context)).thenReturn("value");

        assertSame(notFound, assertThrows(MethodNotFoundException.class, () -> invokeOuterCompositeMethodExpression(context, source, PARAMS, notFound)));
    }

    @Test
    void outerMethodExpressionWithoutMethodThrowsGivenMethodNotFoundException() {
        when(source.getValue(context)).thenReturn(outer);
        when(outer.invoke(context, PARAMS)).thenThrow(new MethodNotFoundException("outer"));

        assertSame(notFound, assertThrows(MethodNotFoundException.class, () -> invokeOuterCompositeMethodExpression(context, source, PARAMS, notFound)));
    }

    @Test
    void exceptionOfOuterMethodPropagates() {
        ELException thrown = new ELException(new IllegalStateException());
        when(source.getValue(context)).thenReturn(outer);
        when(outer.invoke(context, PARAMS)).thenThrow(thrown);

        assertSame(thrown, assertThrows(ELException.class, () -> invokeOuterCompositeMethodExpression(context, source, PARAMS, notFound)));
    }

    @Test
    void validatorExceptionOfOuterMethodIsUnwrapped() {
        ValidatorException invalid = new ValidatorException(new FacesMessage("invalid"));
        when(source.getValue(context)).thenReturn(outer);
        when(outer.invoke(context, PARAMS)).thenThrow(new ELException(invalid));

        assertSame(invalid, assertThrows(ValidatorException.class, () -> invokeOuterCompositeMethodExpression(context, source, PARAMS, notFound)));
    }
}
