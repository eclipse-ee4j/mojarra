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

package jakarta.faces.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

/**
 * The JUnit tests for CollectionDataModel.
 * 
 * @author Manfred Riem
 */
public class CollectionDataModelTest {

    /**
     * Test getWrappedData method.
     */
    @Test
    public void testGetWrappedData() {
        CollectionDataModel model = new CollectionDataModel();
        assertNull(model.getWrappedData());
        ArrayList<String> list = new ArrayList<String>();
        model.setWrappedData(list);
        assertNotNull(model.getWrappedData());
        model.setWrappedData(null);
        assertNull(model.getWrappedData());
    }
}
