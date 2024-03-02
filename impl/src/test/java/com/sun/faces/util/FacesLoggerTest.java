package com.sun.faces.util;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sun.faces.mock.MockApplication;
import com.sun.faces.mock.MockFacesContext;

import jakarta.el.ELContext;
import jakarta.faces.component.UIViewRoot;

public class FacesLoggerTest {

    @Test
    public void unresolvedNavigationUS() {
        MockFacesContext facesContext = new MockFacesContext();
        facesContext.setApplication(new MockApplication());
        facesContext.setELContext(Mockito.mock(ELContext.class));
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.setLocale(Locale.US);
        facesContext.setViewRoot(root);
        final Object[] params = {"componentId1", "outcome.xhtml", "viewId.xhtml"};
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
        final Object[] params = {"componentId1", "outcome.xhtml", "viewId.xhtml"};
        Locale.setDefault(new Locale("de", "DE"));
        final String result = FacesLogger.RENDERKIT.interpolateMessage(facesContext, "faces.outcometarget.navigation.case.not.resolved", params);
        Assertions.assertEquals("JSF1090: Navigations-Fall wurde für Komponente componentId1 nicht aufgelöst.", result);
    }
}
