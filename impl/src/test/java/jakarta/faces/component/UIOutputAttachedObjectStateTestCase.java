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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.mock.MockExternalContext;
import com.sun.faces.mock.MockFacesContext;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockLifecycle;
import com.sun.faces.mock.MockServletContext;

import jakarta.faces.FactoryFinder;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.DateTimeConverter;

public class UIOutputAttachedObjectStateTestCase {

    private MockFacesContext facesContext = null;
    private MockServletContext servletContext;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() throws Exception {
        facesContext = new MockFacesContext();
        facesContext = new MockFacesContext();

        servletContext = new MockServletContext();
        servletContext.addInitParameter("appParamName", "appParamValue");
        servletContext.setAttribute("appScopeName", "appScopeValue");
        request = new MockHttpServletRequest(null);
        request.setAttribute("reqScopeName", "reqScopeValue");
        response = new MockHttpServletResponse();

        // Create something to stand-in as the InitFacesContext
        new MockFacesContext(new MockExternalContext(servletContext, request, response), new MockLifecycle());

    }

    @AfterEach
    protected void tearDown() throws Exception {
        FactoryFinder.releaseFactories();
        Method reInitializeFactoryManager = FactoryFinder.class.getDeclaredMethod("reInitializeFactoryManager", (Class<?>[]) null);
        reInitializeFactoryManager.setAccessible(true);
        reInitializeFactoryManager.invoke(null, (Object[]) null);
    }

    // ------------------------------------------------------------ Test Methods

    @Test
    public void testConverterState() {
        UIOutput output = new UIOutput();
        DateTimeConverter converter = new DateTimeConverter();
        converter.setPattern("MM-dd-yy");
        output.setConverter(converter);
        output.markInitialState();
        assertTrue(output.initialStateMarked());
        assertTrue(converter.initialStateMarked());

        Object result = output.saveState(facesContext);
        // initial state has been marked an no changes
        // have occurred, we should have null state.
        assertNull(result);

        // setup the scenario again, but this time,
        // update the converter pattern.
        output = new UIOutput();
        converter = new DateTimeConverter();
        converter.setPattern("MM-dd-yy");
        output.setConverter(converter);
        output.markInitialState();
        assertTrue(output.initialStateMarked());
        assertTrue(converter.initialStateMarked());

        // now tweak the converter
        converter.setPattern("dd-MM-yy");
        result = output.saveState(facesContext);
        assertTrue(result instanceof Object[]);
        Object[] state = (Object[]) result;

        // state should have a lenght of 2. The first element
        // is the state from UIComponentBase, where the second
        // is the converter state. The first element in this
        // case should be null
        assertTrue(state.length == 2);
        assertTrue(state[0] == null);
        assertTrue(state[1] != null);

        output = new UIOutput();
        converter = new DateTimeConverter();
        output.setConverter(converter);

        // now validate what we've restored
        // first, ensure converter is null. This will
        // be the case when initialState has been marked
        // for the component.
        output.restoreState(facesContext, state);
        assertTrue(output.getConverter() != null);
        assertEquals("dd-MM-yy", converter.getPattern());

        // now validate the case where UIOutput has some event
        // that adds a converter *after* initial state has been
        // marked. This will cause the component to save full
        // state.
        output = new UIOutput();
        output.markInitialState();
        output.setConverter(converter);
        assertTrue(!output.initialStateMarked());
        assertTrue(!converter.initialStateMarked());

        result = output.saveState(facesContext);
        assertNotNull(result);

        // this time, both elements in the state array will not
        // be null. If we call retoreState() on a new component instance
        // without setting a converter, we should have a new DateTimeConverter
        // *with* the expected pattern.
        assertTrue(result instanceof Object[]);
        state = (Object[]) result;
        assertTrue(state.length == 2);
        assertTrue(state[1] instanceof StateHolderSaver);
        output = new UIOutput();
        assertNull(output.getConverter());
        output.restoreState(facesContext, state);
        Converter c = output.getConverter();
        assertNotNull(c);
        assertTrue(c instanceof DateTimeConverter);
        converter = (DateTimeConverter) c;
        assertEquals("dd-MM-yy", converter.getPattern());
    }
}
