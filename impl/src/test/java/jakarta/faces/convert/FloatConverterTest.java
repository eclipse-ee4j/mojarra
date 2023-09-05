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

package jakarta.faces.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import jakarta.faces.component.UIPanel;
import jakarta.faces.context.FacesContext;

/**
 * The JUnit tests for the FloatConverter class.
 */
public class FloatConverterTest {

    /**
     * Test getAsObject method.
     */
    @Test(expected = NullPointerException.class)
    public void testGetAsObject() {
        FloatConverter converter = new FloatConverter();
        converter.getAsObject(null, null, null);
    }

    /**
     * Test getAsObject method.
     */
    @Test
    public void testGetAsObject2() {
        FloatConverter converter = new FloatConverter();
        FacesContext facesContext = PowerMock.createMock(FacesContext.class);
        replay(facesContext);
        assertNull(converter.getAsObject(facesContext, new UIPanel(), null));
        verify(facesContext);
    }

    /**
     * Test getAsObject method.
     */
    @Test
    public void testGetAsObject3() {
        FloatConverter converter = new FloatConverter();
        FacesContext facesContext = PowerMock.createMock(FacesContext.class);
        replay(facesContext);
        assertNull(converter.getAsObject(facesContext, new UIPanel(), "     "));
        verify(facesContext);
    }

    /**
     * Test getAsObject method.
     */
    @Test
    public void testGetAsObject4() {
        FloatConverter converter = new FloatConverter();
        FacesContext facesContext = PowerMock.createMock(FacesContext.class);
        replay(facesContext);
        assertEquals(Float.valueOf("12.3"), converter.getAsObject(facesContext, new UIPanel(), "12.3"));
        verify(facesContext);
    }

    /**
     * Test getAsString method.
     */
    @Test(expected = NullPointerException.class)
    public void testGetAsString() {
        FloatConverter converter = new FloatConverter();
        converter.getAsString(null, null, null);
    }

    /**
     * Test getAsString method.
     */
    @Test
    public void testGetAsString2() {
        FloatConverter converter = new FloatConverter();
        FacesContext facesContext = PowerMock.createMock(FacesContext.class);
        replay(facesContext);
        assertEquals("", converter.getAsString(facesContext, new UIPanel(), null));
        verify(facesContext);
    }

    /**
     * Test getAsString method.
     */
    @Test
    public void testGetAsString3() {
        FloatConverter converter = new FloatConverter();
        FacesContext facesContext = PowerMock.createMock(FacesContext.class);
        replay(facesContext);
        assertEquals("12.3", converter.getAsString(facesContext, new UIPanel(), 12.3f));
        verify(facesContext);
    }

    /**
     * Test getAsString method.
     */
    @Test
    public void testGetAsString4() {
        FloatConverter converter = new FloatConverter();
        FacesContext facesContext = PowerMock.createMock(FacesContext.class);
        replay(facesContext);
        assertEquals("12.3", converter.getAsString(facesContext, new UIPanel(), Float.valueOf("12.3")));
        verify(facesContext);
    }
}
