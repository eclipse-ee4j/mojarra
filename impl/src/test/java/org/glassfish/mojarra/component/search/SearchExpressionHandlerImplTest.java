/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.glassfish.mojarra.component.search;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class SearchExpressionHandlerImplTest {

    @Mock
    private FacesContext mockedFacesContext;

    private SearchExpressionHandlerImpl handler = new SearchExpressionHandlerImpl();

    @Test
    public void testSplitSpaceSeparatedExpressions() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, "@this that");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitSpaceSeparatedExpressionsWithLeadingSpace() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, " @this that");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitSpaceSeparatedExpressionsWithTrailingSpace() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, "@this that ");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitSpaceSeparatedExpressionsWithDoubleSpace() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, "@this  that");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitCommaSeparatedExpressions() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, "@this,that");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitCommaSeparatedExpressionsWithLeadingComma() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, ",@this,that");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitCommaSeparatedExpressionsWithTrailingComma() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, "@this,that,");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitCommaSeparatedExpressionsWithDoubleComma() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, "@this,,that");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitCommaSpaceSeparatedExpressions() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, "@this, that");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitCommaSpaceSeparatedExpressionsWithLeadingCommaSpace() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, ", @this, that");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitCommaSpaceSeparatedExpressionsWithTrailingCommaSpace() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, "@this, that, ");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

    @Test
    public void testSplitCommaSpaceSeparatedExpressionsWithDoubleCommaSpace() {
        String[] expressions = handler.splitExpressions(mockedFacesContext, "@this, , that");
        assertEquals(List.of("@this", "that"), asList(expressions));
    }

}
