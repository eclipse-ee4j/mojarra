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

package org.glassfish.mojarra.facelets.tag.jstl.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.event.PhaseId;

import org.glassfish.mojarra.junit.JUnitFacesTestCaseBase;
import org.junit.jupiter.api.Test;

class IterationBaseCacheTest extends JUnitFacesTestCaseBase {

    private final ValueExpression items = mock(ValueExpression.class);
    private final ELContext elContext = mock(ELContext.class);

    @Test
    void aValueAlreadyResolvedIsNotResolvedAgainInTheSamePhase() {
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        List<String> resolved = List.of("a");

        IterationBaseCache.put(facesContext, items, resolved);

        assertEquals(resolved, IterationBaseCache.getValue(elContext, items));
        verify(items, never()).getValue(any(ELContext.class));
    }

    @Test
    void aResolvedNullIsNotResolvedAgainInTheSamePhase() {
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);

        IterationBaseCache.put(facesContext, items, null);

        assertNull(IterationBaseCache.getValue(elContext, items));
        verify(items, never()).getValue(any(ELContext.class));
    }

    @Test
    void aValueResolvedInAnEarlierPhaseIsResolvedAgain() {
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        IterationBaseCache.put(facesContext, items, List.of("old"));
        List<String> current = List.of("new");
        when(items.getValue(elContext)).thenReturn(current);

        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);

        assertEquals(current, IterationBaseCache.getValue(elContext, items));
    }

}
