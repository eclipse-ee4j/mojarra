package com.sun.faces.component.search;

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
