package org.glassfish.mojarra.util;

import java.util.Locale;

import jakarta.el.ELContext;
import jakarta.faces.component.UIViewRoot;

import org.glassfish.mojarra.mock.MockApplication;
import org.glassfish.mojarra.mock.MockFacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class FacesLoggerTest {

    private Locale previousDefaultLocale;

    @BeforeEach
    public void setUp() {
        previousDefaultLocale = Locale.getDefault();
    }

    @AfterEach
    public void tearDown() {
        Locale.setDefault(previousDefaultLocale);
    }

    @Test
    public void unresolvedNavigationUS() {
        MockFacesContext facesContext = new MockFacesContext();
        facesContext.setApplication(new MockApplication());
        facesContext.setELContext(Mockito.mock(ELContext.class));
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.setLocale(Locale.US);
        facesContext.setViewRoot(root);
        final Object[] params = { "componentId1", "outcome.xhtml", "viewId.xhtml" };
        Locale.setDefault(new Locale("en", "US"));
        final String result = FacesLogger.RENDERKIT.interpolateMessage(facesContext, "faces.outcometarget.navigation.case.not.resolved", params);
        Assertions.assertEquals("JSF1090: Navigation case outcome.xhtml not resolved for component componentId1 in viewId viewId.xhtml", result);
    }

    @Test
    public void unresolvedNavigationGermany() {
        MockFacesContext facesContext = new MockFacesContext();
        facesContext.setApplication(new MockApplication());
        facesContext.setELContext(Mockito.mock(ELContext.class));
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.setLocale(Locale.GERMANY);
        facesContext.setViewRoot(root);
        final Object[] params = { "componentId1", "outcome.xhtml", "viewId.xhtml" };
        Locale.setDefault(new Locale("de", "DE"));
        final String result = FacesLogger.RENDERKIT.interpolateMessage(facesContext, "faces.outcometarget.navigation.case.not.resolved", params);
        Assertions.assertEquals("JSF1090: Navigations-Fall wurde für Komponente componentId1 nicht aufgelöst.", result);
    }

}
