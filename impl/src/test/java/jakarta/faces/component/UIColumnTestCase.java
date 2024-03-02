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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Unit tests for {@link UIColumn}.
 * </p>
 */
public class UIColumnTestCase extends UIComponentBaseTestCase {

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        component = new UIColumn();
        expectedFamily = UIColumn.COMPONENT_FAMILY;
        expectedId = null;
        expectedRendererType = null;
    }

    // Tear down instance variables required by ths test case
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods
    // Suppress lifecycle tests since we do not have a renderer
    @Override
    @Test
    public void testLifecycleManagement() {
    }

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UIColumn();
        component.setRendererType(null);
        return component;
    }
}
