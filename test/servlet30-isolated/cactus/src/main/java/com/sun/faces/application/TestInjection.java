/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.application;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.spi.InjectionProvider;

public class TestInjection extends ServletFacesTestCase {

    public TestInjection() {
        super("TestInjection");
    }


    public TestInjection(String name) {
        super(name);
    }


    public void setUp() {
        super.setUp();
    }


    // ------------------------------------------------------------ Test Methods

    /**
     * Validate PostConstruct/PreDestroy annotations are property
     * invoked on protected, package private, and private methods.
     * @throws Exception if an error occurs
     */
    public void testInjection() throws Exception {
        ProtectedBean protectedBean = new ProtectedBean();
        PackagePrivateBean packagePrivateBean = new PackagePrivateBean();
        PrivateBean privateBean = new PrivateBean();
        ConcreteBean concreteBean = new ConcreteBean();

        ApplicationFactory aFactory =
              (ApplicationFactory) FactoryFinder.getFactory(
                    FactoryFinder.APPLICATION_FACTORY);
        aFactory.getApplication(); // bootstraps the ApplicationAssociate    
        ApplicationAssociate associate = ApplicationAssociate
              .getInstance(getFacesContext().getExternalContext());        
        assertNotNull(associate);       
        
        InjectionProvider injectionProvider = associate.getInjectionProvider();
        assertNotNull(injectionProvider);
        try {
            injectionProvider.inject(protectedBean);
            injectionProvider.invokePostConstruct(protectedBean);
            injectionProvider.invokePreDestroy(protectedBean);
            injectionProvider.inject(packagePrivateBean);
            injectionProvider.invokePostConstruct(packagePrivateBean);
            injectionProvider.invokePreDestroy(packagePrivateBean);
            injectionProvider.inject(privateBean);
            injectionProvider.invokePostConstruct(privateBean);
            injectionProvider.invokePreDestroy(privateBean);
            injectionProvider.inject(concreteBean);
            injectionProvider.invokePostConstruct(concreteBean);
            injectionProvider.invokePreDestroy(concreteBean);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            assertTrue(false);
        }
        
        assertTrue(protectedBean.getInit());
        assertTrue(protectedBean.getDestroy());
        assertTrue(packagePrivateBean.getInit());
        assertTrue(packagePrivateBean.getDestroy());
        assertTrue(privateBean.getInit());
        assertTrue(privateBean.getDestroy());
        assertTrue(concreteBean.getInit());
        assertTrue(concreteBean.getDestroy());
    }


    // ----------------------------------------------------------- Inner Classes

    private static class ProtectedBean {

        private boolean initCalled;
        private boolean destroyCalled;

        @PostConstruct void init() {
            initCalled = true;
        }

        @PreDestroy void destroy() {
            destroyCalled = true;
        }

        public boolean getInit() {
            return initCalled;
        }

        public boolean getDestroy() {
            return destroyCalled;
        }

    } // END ProtectedBean
    
    private static class PackagePrivateBean {

        private boolean initCalled;
        private boolean destroyCalled;

        @PostConstruct void init() {
            initCalled = true;
        }

        @PreDestroy void destroy() {
            destroyCalled = true;
        }

        public boolean getInit() {
            return initCalled;
        }

        public boolean getDestroy() {
            return destroyCalled;
        }

    } // END PackagePrivateBean
    
    private static class PrivateBean {

        private boolean initCalled;
        private boolean destroyCalled;

        @PostConstruct void init() {
            initCalled = true;
        }

        @PreDestroy void destroy() {
            destroyCalled = true;
        }

        public boolean getInit() {
            return initCalled;
        }

        public boolean getDestroy() {
            return destroyCalled;
        }

    } // END PrivateBean
    
    private static abstract class BaseBean {
        
        protected boolean initCalled;
        protected boolean destroyCalled;
        
        @PostConstruct void init() {
            initCalled = true;
        }
    }
    
    private static class ConcreteBean extends BaseBean {
        
        @PreDestroy void destroy() {
            destroyCalled = true;
        }
        
        public boolean getInit() {
            return initCalled;
        }

        public boolean getDestroy() {
            return destroyCalled;
        }
    }

} // END TestInjection
