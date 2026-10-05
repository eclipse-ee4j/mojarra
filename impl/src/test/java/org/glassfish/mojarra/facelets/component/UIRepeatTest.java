/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.mojarra.facelets.component;

import static jakarta.faces.component.visit.VisitHint.SKIP_UNRENDERED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UIRepeatTest {

    private FacesContext ctx;

    private FacesMessage.Severity maximumSeverity = FacesMessage.Severity.WARN;

    private Method uiRepeatHasErrorMessages;

    @Test
    public void testVarAndVarStatusRejectValueExpression() {
        UIRepeat repeat = new UIRepeat();
        ValueExpression expression = Mockito.mock(ValueExpression.class);

        assertThrows(IllegalArgumentException.class, () -> repeat.setValueExpression("var", expression));
        assertThrows(IllegalArgumentException.class, () -> repeat.setValueExpression("varStatus", expression));
    }

    @Test
    public void testHasErrorMessages() throws Exception {
        ctx = Mockito.mock(FacesContext.class);
        when(ctx.getMaximumSeverity()).thenAnswer(new Answer<Severity>() {

            @Override
            public Severity answer(InvocationOnMock invocation) throws Throwable {
                return maximumSeverity;
            }

        });

        maximumSeverity = FacesMessage.Severity.WARN;
        assertEquals(false, hasErrorMessages(ctx));
        maximumSeverity = FacesMessage.Severity.INFO;
        assertEquals(false, hasErrorMessages(ctx));
        maximumSeverity = FacesMessage.Severity.ERROR;
        assertEquals(true, hasErrorMessages(ctx));
        maximumSeverity = FacesMessage.Severity.FATAL;
        assertEquals(true, hasErrorMessages(ctx));
    }

    /**
     * The current component pushed before evaluating the <code>rendered</code> property during a tree visit must be popped again when that evaluation throws.
     */
    @Test
    public void visitTreeRestoresCurrentComponentWhenRenderedThrows() {
        ctx = Mockito.mock(FacesContext.class);
        when(ctx.getAttributes()).thenReturn(new HashMap<>());
        VisitContext visitContext = Mockito.mock(VisitContext.class);
        when(visitContext.getFacesContext()).thenReturn(ctx);
        when(visitContext.getHints()).thenReturn(EnumSet.of(SKIP_UNRENDERED));
        UIPanel parent = new UIPanel();
        UIRepeat repeat = new UIRepeat() {

            @Override
            public boolean isRendered() {
                throw new IllegalStateException();
            }

        };
        parent.getChildren().add(repeat);
        parent.pushComponentToEL(ctx, null);

        assertThrows(IllegalStateException.class, () -> repeat.visitTree(visitContext, (context, target) -> null));
        assertSame(parent, UIComponent.getCurrentComponent(ctx));
    }

    private boolean hasErrorMessages(FacesContext context) throws Exception {
        if (uiRepeatHasErrorMessages == null) {
            Class<?> uiRepeatClass = Class.forName(UIRepeat.class.getName());
            uiRepeatHasErrorMessages = uiRepeatClass.getDeclaredMethod(
                "hasErrorMessages", new Class<?>[] { FacesContext.class }
            );
            uiRepeatHasErrorMessages.setAccessible(true);
        }
        return (Boolean) uiRepeatHasErrorMessages.invoke(
            new UIRepeat(),
            new Object[] { context }
        );
    }

}
