/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package com.sun.faces.component.visit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.sun.faces.mock.MockApplication;
import com.sun.faces.mock.MockFacesContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;

/**
 * The number of naming-container separators in a client id equals its naming-container nesting depth, which real
 * views keep to a handful. PartialVisitContext registers one ancestor-subtree entry per separator, so the registered
 * depth is bounded to keep an id built from many separators from growing the map out of proportion, while the full
 * client id itself is still kept for the visit.
 */
class PartialVisitContextDepthTest {

    private static final int SEPARATORS = 100_000;
    private static final int MAX_REGISTERED_DEPTH = 64;

    @AfterEach
    void tearDown() throws Exception {
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, new Object[] { null });
    }

    private static FacesContext contextWithDefaultSeparator() {
        MockFacesContext context = new MockFacesContext();
        context.setApplication(new MockApplication());
        context.getAttributes().put(UINamingContainer.SEPARATOR_CHAR_PARAM_NAME, UINamingContainer.SEPARATOR_CHAR);
        return context;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> subtreeClientIds(PartialVisitContext visitContext) throws Exception {
        Field field = PartialVisitContext.class.getDeclaredField("subtreeClientIds");
        field.setAccessible(true);
        return (Map<String, ?>) field.get(visitContext);
    }

    @Test
    void registeredSubtreeDepthIsBoundedForAnIdOfManySeparators() throws Exception {
        String clientId = String.valueOf(UINamingContainer.SEPARATOR_CHAR).repeat(SEPARATORS);
        PartialVisitContext visitContext = new PartialVisitContext(contextWithDefaultSeparator(), List.of(clientId));

        assertEquals(MAX_REGISTERED_DEPTH, subtreeClientIds(visitContext).size(),
                "registered ancestor-subtree depth must stay bounded");
        assertTrue(visitContext.getIdsToVisit().contains(clientId), "the full client id must still be kept");
    }

    @Test
    void everySeparatorIsRegisteredWhenBelowTheBound() throws Exception {
        char s = UINamingContainer.SEPARATOR_CHAR;
        String clientId = "a" + s + "b" + s + "c" + s + "d";
        PartialVisitContext visitContext = new PartialVisitContext(contextWithDefaultSeparator(), List.of(clientId));

        assertEquals(3, subtreeClientIds(visitContext).size());
    }
}
