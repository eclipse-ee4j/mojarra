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

package jakarta.faces;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class FactoryFinderTestCase2 extends TestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public FactoryFinderTestCase2(String name) {
        super(name);
    }
    
        public static String FACTORIES[][] = {
	{ FactoryFinder.APPLICATION_FACTORY, 
	  "com.sun.faces.mock.MockApplicationFactory"
	},
	{ FactoryFinder.EXTERNAL_CONTEXT_FACTORY, 
	  "com.sun.faces.mock.MockExternalContextFactory"
	},
	{ FactoryFinder.FACES_CONTEXT_FACTORY, 
	  "com.sun.faces.mock.MockFacesContextFactory"
	},
	{ FactoryFinder.LIFECYCLE_FACTORY, 
	  "com.sun.faces.mock.MockLifecycleFactory"
	},
	{ FactoryFinder.RENDER_KIT_FACTORY, 
	  "com.sun.faces.mock.MockRenderKitFactory"
	}
    };

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        super.setUp();
        for (int i = 0, len = FactoryFinderTestCase2.FACTORIES.length; i < len; i++) {
            System.getProperties().remove(FactoryFinderTestCase2.FACTORIES[i][0]);
        }
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(FactoryFinderTestCase2.class));
    }

    // Tear down instance variables required by ths test case
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FactoryFinder.releaseFactories();
        for (int i = 0, len = FactoryFinderTestCase2.FACTORIES.length; i < len; i++) {
            System.getProperties().remove(FactoryFinderTestCase2.FACTORIES[i][0]);
        }
    }

    // ------------------------------------------------- Individual Test Methods
    /**
     * <p>
     * In the absence of webapp faces-config.xml and META-INF/services, verify
     * that the overrides specified in the implementation faces-config.xml take
     * precedence.</p>
     * @throws java.lang.Exception
     */
    public void testJSFImplCase() throws Exception {
        Object factory = null;
        Class<?> clazz = null;

        FactoryFinder.releaseFactories();
        int len, i = 0;

	// this testcase only simulates the "faces implementation
        // specific" part
        for (i = 0, len = FactoryFinderTestCase2.FACTORIES.length; i < len; i++) {
            FactoryFinder.setFactory(FactoryFinderTestCase2.FACTORIES[i][0],
                    FactoryFinderTestCase2.FACTORIES[i][1]);
        }

        for (i = 0, len = FactoryFinderTestCase2.FACTORIES.length; i < len; i++) {
            clazz = Class.forName(FactoryFinderTestCase2.FACTORIES[i][0]);
            factory = FactoryFinder.getFactory(FactoryFinderTestCase2.FACTORIES[i][0]);
            assertTrue("Factory for " + clazz.getName()
                    + " not of expected type.",
                    clazz.isAssignableFrom(factory.getClass()));
            clazz = Class.forName(FactoryFinderTestCase2.FACTORIES[i][1]);
            assertTrue("Factory " + FactoryFinderTestCase2.FACTORIES[i][1] + " not of expected type",
                    clazz.isAssignableFrom(factory.getClass()));
        }
    }
}
