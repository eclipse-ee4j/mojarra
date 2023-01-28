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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link ListDataModel}.</p>
 */
public class ListDataModelTestCase extends DataModelTestCaseBase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ListDataModelTestCase(String name) {

        super(name);

    }

    // ------------------------------------------------------ Instance Variables
    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        List<BeanTestImpl> list = new ArrayList<BeanTestImpl>();
        for (int i = 0; i < 5; i++) {
            list.add(new BeanTestImpl());
        }
        beans = list.toArray(new BeanTestImpl[5]);
        configure();
        model = new ListDataModel<BeanTestImpl>(list);
        super.setUp();
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(ListDataModelTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    // ------------------------------------------------------- Protected Methods
}
