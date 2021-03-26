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

package com.sun.faces.el;

import org.junit.Test;

import com.sun.faces.mock.MockApplication;
import com.sun.faces.mock.MockExternalContext;
import com.sun.faces.mock.MockFacesContext;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockServletContext;

import junit.framework.TestCase;

public class ImplicitObjectELResolverTest extends TestCase {

    @Test
    public void testGetValueForCC() throws Exception {

        MockFacesContext mockFacesContext = new MockFacesContext(
            new MockExternalContext(
                new MockServletContext(),
                new MockHttpServletRequest(), 
                new MockHttpServletResponse()));
        mockFacesContext.setApplication(new MockApplication());

        new ImplicitObjectELResolver().getValue(mockFacesContext.getELContext(), null, "cc");

        assertTrue(mockFacesContext.getELContext().isPropertyResolved());
    }

}
