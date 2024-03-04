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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockServletContext;

import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;

public class FactoryFinderTestCase {

    public static String FACTORIES[][] = {
        {FactoryFinder.APPLICATION_FACTORY,
            "com.sun.faces.mock.MockApplicationFactory"
        },
        {FactoryFinder.EXTERNAL_CONTEXT_FACTORY,
            "com.sun.faces.mock.MockExternalContextFactory"
        },
        {FactoryFinder.FACES_CONTEXT_FACTORY,
            "com.sun.faces.mock.MockFacesContextFactory"
        },
        {FactoryFinder.LIFECYCLE_FACTORY,
            "com.sun.faces.mock.MockLifecycleFactory"
        },
        {FactoryFinder.RENDER_KIT_FACTORY,
            "com.sun.faces.mock.MockRenderKitFactory"
        }
    };

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @BeforeEach
    public void setUp() throws Exception {
        Method method = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        method.setAccessible(true);
        method.invoke(null, new Object[]{null});

        for (int i = 0, len = FACTORIES.length; i < len; i++) {
            System.getProperties().remove(FACTORIES[i][0]);
        }
    }

    // Tear down instance variables required by ths test case
    @AfterEach
    public void tearDown() throws Exception {
        FactoryFinder.releaseFactories();
        for (int i = 0, len = FACTORIES.length; i < len; i++) {
            System.getProperties().remove(FACTORIES[i][0]);
        }
    }

    // ------------------------------------------------- Individual Test Methods
    /**
     * <p>
     * verify that the overrides specified in the faces-config.xml in the user's
     * webapp take precedence.</p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testFacesConfigCase() throws Exception {
        Object factory = null;
        Class<?> clazz = null;

        FactoryFinder.releaseFactories();
        int len, i = 0;

        // simulate the "faces implementation specific" part
        for (i = 0, len = FACTORIES.length; i < len; i++) {
            FactoryFinder.setFactory(FACTORIES[i][0],
                    FACTORIES[i][1]);
        }

        // simulate the "WEB-INF/services" part
        // this is done by the build.xml file
        File servicesDir = new File(System.getProperty("basedir"), "target/classes/META-INF/services");
        servicesDir.mkdirs();

        File servicesFile = new File(servicesDir, "jakarta.faces.context.FacesContextFactory");

        if (servicesFile.exists()) {
            servicesFile.delete();
        }
        PrintWriter writer = new PrintWriter(servicesFile);
        writer.println("jakarta.faces.mock.MockFacesContextFactoryExtender");
        writer.flush();
        writer.close();

        File cServicesDir = new File(System.getProperty("basedir"), "target/generated-classes/cobertura/META-INF/services");
        cServicesDir.mkdirs();

        File cServicesFile = new File(cServicesDir, "jakarta.faces.context.FacesContextFactory");

        if (cServicesFile.exists()) {
            cServicesFile.delete();
        }
        PrintWriter cWriter = new PrintWriter(cServicesFile);
        cWriter.println("jakarta.faces.mock.MockFacesContextFactoryExtender");
        cWriter.flush();
        cWriter.close();

        // simulate the "webapp faces-config.xml" part
        FactoryFinder.setFactory(FactoryFinder.FACES_CONTEXT_FACTORY,
                "jakarta.faces.mock.MockFacesContextFactoryExtender2");

        for (i = 0, len = FACTORIES.length; i < len; i++) {
            clazz = Class.forName(FACTORIES[i][0]);
            factory = FactoryFinder.getFactory(FACTORIES[i][0]);
            assertTrue(
                    clazz.isAssignableFrom(factory.getClass()), "Factory for " + clazz.getName()
                    + " not of expected type.");
            clazz = Class.forName(FACTORIES[i][1]);
            assertTrue(
                    clazz.isAssignableFrom(factory.getClass()), "Factory " + FACTORIES[i][1] + " not of expected type");

        }
        // verify that the delegation works
        assertTrue(System.getProperty(FACTORIES[2][0]).equals("jakarta.faces.mock.MockFacesContextFactoryExtender2"));
        assertTrue(System.getProperty("oldImpl").equals("jakarta.faces.mock.MockFacesContextFactoryExtender"));

        // Verify IllegalStateException when factory not found
        FactoryFinder.releaseFactories();
        FactoryFinder.setFactory(FACTORIES[0][0], FACTORIES[0][1]);
        FactoryFinder.setFactory(FACTORIES[1][0], FACTORIES[1][1]);
        FactoryFinder.setFactory(FACTORIES[2][0], FACTORIES[2][1]);
        FactoryFinder.setFactory(FACTORIES[4][0], FACTORIES[4][1]);
        boolean exceptionThrown = false;
        try {
            factory = FactoryFinder.getFactory(FACTORIES[3][0]);
        } catch (IllegalStateException ise) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        servicesFile.delete();
        cServicesFile.delete();
    }

    // TODO re-enable
    @Test
    public void testNoFacesContext() throws Exception {
//        assertNull(FacesContext.getCurrentInstance());
//        Object result = FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
//        assertNotNull(result);
//        assertTrue(result instanceof FacesContextFactory);
    }

    /**
     * <p>
     * In the absence of webapp faces-config.xml, verify that the overrides
     * specified in the META-INF/services take precedence.</p>
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testServicesCase() throws Exception {
        Object factory = null;
        Class<?> clazz = null;

        FactoryFinder.releaseFactories();
        int len, i = 0;

        // simulate the "faces implementation specific" part
        for (i = 0, len = FACTORIES.length; i < len; i++) {
            FactoryFinder.setFactory(FACTORIES[i][0],
                    FACTORIES[i][1]);
        }

        // simulate the "WEB-INF/services" part
        // this is done by the build.xml file
        File servicesDir = new File(System.getProperty("basedir"), "target/classes/META-INF/services");
        servicesDir.mkdirs();

        File servicesFile = new File(servicesDir, "jakarta.faces.context.FacesContextFactory");

        if (servicesFile.exists()) {
            servicesFile.delete();
        }

        PrintWriter writer = new PrintWriter(servicesFile);
        writer.println("jakarta.faces.mock.MockFacesContextFactoryExtender");
        writer.flush();
        writer.close();

        File cServicesDir = new File(System.getProperty("basedir"), "target/generated-classes/cobertura/META-INF/services");
        cServicesDir.mkdirs();

        File cServicesFile = new File(cServicesDir, "jakarta.faces.context.FacesContextFactory");

        if (cServicesFile.exists()) {
            cServicesFile.delete();
        }

        PrintWriter cWriter = new PrintWriter(cServicesFile);
        cWriter.println("jakarta.faces.mock.MockFacesContextFactoryExtender");
        cWriter.flush();
        cWriter.close();

        // this testcase omits the "webapp faces-config.xml" simulation
        for (i = 0, len = FACTORIES.length; i < len; i++) {
            clazz = Class.forName(FACTORIES[i][0]);
            factory = FactoryFinder.getFactory(FACTORIES[i][0]);
            assertTrue(
                    clazz.isAssignableFrom(factory.getClass()), "Factory for " + clazz.getName()
                    + " not of expected type.");
            clazz = Class.forName(FACTORIES[i][1]);
            assertTrue(
                    clazz.isAssignableFrom(factory.getClass()), "Factory " + FACTORIES[i][1] + " not of expected type");

        }
        // verify that the delegation works
        assertTrue(System.getProperty(FACTORIES[2][0]).equals("jakarta.faces.mock.MockFacesContextFactoryExtender"));
        assertTrue(System.getProperty("oldImpl").equals("com.sun.faces.mock.MockFacesContextFactory"));

        servicesFile.delete();
        cServicesFile.delete();
    }

    @Test
    public void testNoFacesContextInitially() throws Exception {
        assertNull(FacesContext.getCurrentInstance());

        FactoryFinder.releaseFactories();
        FactoryFinder.setFactory(FACTORIES[0][0], FACTORIES[0][1]);
        FactoryFinder.setFactory(FACTORIES[1][0], FACTORIES[1][1]);
        FactoryFinder.setFactory(FACTORIES[2][0], FACTORIES[2][1]);
        FactoryFinder.setFactory(FACTORIES[3][0], FACTORIES[3][1]);
        FactoryFinder.setFactory(FACTORIES[4][0], FACTORIES[4][1]);

        FacesContextFactory fcFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        LifecycleFactory lFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Object request = new MockHttpServletRequest();
        Object response = new MockHttpServletResponse();
        Object containerContext = new MockServletContext();
        Lifecycle l = lFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        FacesContext context = fcFactory.getFacesContext(containerContext, request, response, l);

        ApplicationFactory aFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application app = aFactory.getApplication();
        FactoryFinder.releaseFactories();
    }

    // ------------------------------------------- helpers
    public static void printRelevantSystemProperties() {
        System.out.println("++++++Relevant System Properties: ");
        for (int i = 0, len = FACTORIES.length; i < len; i++) {
            System.out.println(FACTORIES[i][0] + ": "
                    + System.getProperty(FACTORIES[i][0]));
        }
    }
}
