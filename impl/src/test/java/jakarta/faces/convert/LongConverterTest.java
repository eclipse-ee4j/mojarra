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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.faces.component.UIPanel;
import jakarta.faces.context.FacesContext;

/**
 * The JUnit tests for the LongConverter class.
 */
public class LongConverterTest {

    /**
     * Test getAsObject method.
     */
    @Test
    public void testGetAsObject() {
        LongConverter converter = new LongConverter();
        assertThrows(NullPointerException.class, () -> converter.getAsObject(null, null, null));
    }

    /**
     * Test getAsObject method.
     */
    @Test
    public void testGetAsObject2() {
        LongConverter converter = new LongConverter();
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        assertNull(converter.getAsObject(facesContext, new UIPanel(), null));
    }

    /**
     * Test getAsObject method.
     */
    @Test
    public void testGetAsObject3() {
        LongConverter converter = new LongConverter();
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        assertNull(converter.getAsObject(facesContext, new UIPanel(), "     "));
    }

    /**
     * Test getAsObject method.
     */
    @Test
    public void testGetAsObject4() {
        LongConverter converter = new LongConverter();
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        assertEquals(Long.valueOf("123"), converter.getAsObject(facesContext, new UIPanel(), "123"));
    }

    /**
     * Test getAsString method.
     */
    @Test
    public void testGetAsString() {
        LongConverter converter = new LongConverter();
        assertThrows(NullPointerException.class, () -> converter.getAsString(null, null, null));
    }

    /**
     * Test getAsString method.
     */
    @Test
    public void testGetAsString2() {
        LongConverter converter = new LongConverter();
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        assertEquals("", converter.getAsString(facesContext, new UIPanel(), null));
    }

    /**
     * Test getAsString method.
     */
    @Test
    public void testGetAsString3() {
        LongConverter converter = new LongConverter();
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        assertEquals("123", converter.getAsString(facesContext, new UIPanel(), "123"));
    }

    /**
     * Test getAsString method.
     */
    @Test
    public void testGetAsString4() {
        LongConverter converter = new LongConverter();
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        assertEquals("123", converter.getAsString(facesContext, new UIPanel(), Long.valueOf("123")));
    }
}
