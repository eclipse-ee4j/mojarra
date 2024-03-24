/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostConstructViewMapEvent;
import jakarta.faces.event.PreDestroyViewMapEvent;
import jakarta.servlet.http.HttpSession;

public class UIViewRootTest {

    @Test
    public void testViewMapPostConstructViewMapEvent() {
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        Application application = Mockito.mock(Application.class);
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);
        HttpSession httpSession = Mockito.mock(HttpSession.class);

        setFacesContext(facesContext);

        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getApplicationMap()).thenReturn(null);
        UIViewRoot viewRoot = new UIViewRoot();

        when(facesContext.getApplication()).thenReturn(application);
        when(application.getProjectStage()).thenReturn(ProjectStage.UnitTest);
        application.publishEvent(facesContext, PostConstructViewMapEvent.class, UIViewRoot.class, viewRoot);
        Map<String, Object> viewMap = viewRoot.getViewMap();
        assertNotNull(viewMap);

        setFacesContext(null);
    }

    @Test
    public void testViewMapPreDestroyViewMapEvent() {
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        Application application = Mockito.mock(Application.class);
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);
        HttpSession httpSession = Mockito.mock(HttpSession.class);

        setFacesContext(facesContext);

        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getApplicationMap()).thenReturn(null);
        UIViewRoot viewRoot = new UIViewRoot();

        when(facesContext.getApplication()).thenReturn(application);
        when(application.getProjectStage()).thenReturn(ProjectStage.UnitTest);
        application.publishEvent(facesContext, PostConstructViewMapEvent.class, UIViewRoot.class, viewRoot);
        when(facesContext.getViewRoot()).thenReturn(viewRoot);
        application.publishEvent(facesContext, PreDestroyViewMapEvent.class, UIViewRoot.class, viewRoot);
        when(facesContext.getViewRoot()).thenReturn(viewRoot);
        application.publishEvent(facesContext, PreDestroyViewMapEvent.class, UIViewRoot.class, viewRoot);


        Map<String, Object> viewMap = viewRoot.getViewMap();
        assertNotNull(viewMap);
        viewRoot.getViewMap().clear();
        viewRoot.getViewMap().clear();


        setFacesContext(null);
    }

    @Test
    public void testViewMapSaveAndRestoreState() {
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        Application application = Mockito.mock(Application.class);
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);
        HttpSession httpSession = Mockito.mock(HttpSession.class);
        HashMap<Object, Object> attributes = new HashMap<>();
        HashMap<String, Object> sessionMap = new HashMap<>();

        setFacesContext(facesContext);

        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getApplicationMap()).thenReturn(null);
        UIViewRoot viewRoot1 = new UIViewRoot();

        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getApplicationMap()).thenReturn(null);
        UIViewRoot viewRoot2 = new UIViewRoot();

        when(facesContext.getAttributes()).thenReturn(attributes);
        when(facesContext.getApplication()).thenReturn(application);
        when(application.getProjectStage()).thenReturn(ProjectStage.UnitTest);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getSessionMap()).thenReturn(sessionMap);
        application.publishEvent(facesContext, PostConstructViewMapEvent.class, UIViewRoot.class, viewRoot1);
        Map<String, Object> viewMap = viewRoot1.getViewMap();
        viewMap.put("one", "one");
        Object saved = viewRoot1.saveState(facesContext);

        /*
         * Simulate our ViewMapListener.
         */
        Map<String, Object> viewMaps = new HashMap<>();
        viewMaps.put((String) viewRoot1.getTransientStateHelper().getTransient("com.sun.faces.application.view.viewMapId"), viewMap);
        sessionMap.put("com.sun.faces.application.view.activeViewMaps", viewMaps);

        viewRoot2.restoreState(facesContext, saved);
        viewMap = viewRoot2.getViewMap();
        assertEquals("one", viewMap.get("one"));

        setFacesContext(null);
    }

    private void setFacesContext(FacesContext facesContext) {
        try {
            Method method = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
            method.setAccessible(true);
            method.invoke(null, facesContext);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }
}
