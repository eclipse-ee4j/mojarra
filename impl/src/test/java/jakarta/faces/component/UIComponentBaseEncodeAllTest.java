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

package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

/**
 * An expression in the <code>rendered</code> attribute can read <code>#{component}</code>, which is the parent during the
 * check of {@link UIComponent#encodeAll(FacesContext)} and the component itself during
 * {@link UIComponentBase#encodeBegin(FacesContext)}. Both evaluations count, so neither may reuse the result of the other.
 * A cache of {@link UIComponent#isRendered()} in {@link UIComponent#encodeAll(FacesContext)} therefore fails this test.
 */
class UIComponentBaseEncodeAllTest extends JUnitFacesTestCaseBase {

    @Test
    @SuppressWarnings("unchecked")
    void encodeBeginEvaluatesRenderedAgainWithTheComponentOnTheElStack() throws Exception {
        Renderer<UIComponent> renderer = mock(Renderer.class);
        UIOutput component = new UIOutput() {
            @Override
            protected Renderer<?> getRenderer(FacesContext context) {
                return renderer;
            }
        };
        UIOutput parent = new UIOutput();
        List<UIComponent> currentComponents = new ArrayList<>();
        ValueExpression rendered = mock(ValueExpression.class);
        when(rendered.getValue(any(ELContext.class))).thenAnswer(invocation -> {
            UIComponent current = UIComponent.getCurrentComponent(facesContext);
            currentComponents.add(current);
            return current != component;
        });
        component.setRendererType("test");
        component.setValueExpression("rendered", rendered);

        parent.pushComponentToEL(facesContext, null);
        component.encodeAll(facesContext);

        assertEquals(List.of(parent, component), currentComponents);
        verify(renderer, never()).encodeBegin(facesContext, component);
    }

}
