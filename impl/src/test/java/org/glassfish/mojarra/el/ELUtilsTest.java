/*
 * Copyright (c) 2019, 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.mojarra.el;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ListResourceBundle;
import java.util.Set;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.context.FacesContext;

import org.glassfish.mojarra.RIConstants;
import org.glassfish.mojarra.application.ApplicationAssociate;
import org.glassfish.mojarra.application.ApplicationFactoryImpl;
import org.glassfish.mojarra.application.ApplicationImpl;
import org.glassfish.mojarra.application.ApplicationResourceBundle;
import org.glassfish.mojarra.context.ExternalContextImpl;
import org.glassfish.mojarra.context.FacesContextImpl;
import org.glassfish.mojarra.lifecycle.LifecycleImpl;
import org.glassfish.mojarra.mock.MockBeanManager;
import org.glassfish.mojarra.mock.MockCDIProvider;
import org.glassfish.mojarra.mock.MockHttpServletRequest;
import org.glassfish.mojarra.mock.MockHttpServletResponse;
import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.el.ExpressionFactoryImpl;

public class ELUtilsTest {

    private ApplicationAssociate applicationAssociate;

    @BeforeEach
    public void setUp() {
        CDI.setCDIProvider(new MockCDIProvider());

        MockServletContext mockServletContext = new MockServletContext() {

            @Override
            public URL getResource(String path) {
                return null;
            }

        };
        mockServletContext.addInitParameter("appParamName", "appParamValue");
        mockServletContext.setAttribute("appScopeName", "appScopeValue");

        ExternalContextImpl externalContext = new ExternalContextImpl(
            mockServletContext,
            new MockHttpServletRequest(),
            new MockHttpServletResponse()
        );

        FactoryFinder.setFactory(
            FactoryFinder.RENDER_KIT_FACTORY,
            "org.glassfish.mojarra.mock.MockRenderKitFactory"
        );

        new FacesContextImpl(externalContext, new LifecycleImpl());
        new ApplicationImpl();

        applicationAssociate = (ApplicationAssociate) externalContext.getApplicationMap()
            .get(RIConstants.RI_PREFIX + "ApplicationAssociate");
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Constructing a FacesContext makes it current for the thread, and FacesContextImpl.release() needs a
        // CDI environment this test has none of, so the protected setter is reached directly.
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, new Object[] { null });

        FactoryFinder.releaseFactories();
    }

    @Test
    public void testNPEWhenStreamELResolverIsNull() {
        // set expr factory with null streamELResolver
        applicationAssociate.setExpressionFactory(new ExpressionFactoryImpl() {

            @Override
            public ELResolver getStreamELResolver() {
                return null;
            }

        });

        DemuxCompositeELResolver elResolver = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);

        ELUtils.buildFacesResolver(elResolver, applicationAssociate); // should not throw NPE
    }

    public static class Messages extends ListResourceBundle {

        @Override
        protected Object[][] getContents() {
            return new Object[][] { { "hello", "Hello" } };
        }

    }

    private static ApplicationResourceBundle messages() {
        return new ApplicationResourceBundle(Messages.class.getName(), null, null);
    }

    @Test
    public void buildFacesResolverPassesTheResourceBundleVarsToTheChain() {
        applicationAssociate.setExpressionFactory(new ExpressionFactoryImpl());
        applicationAssociate.addResourceBundle("msgs", messages());
        Set<?>[] vars = new Set<?>[1];
        DemuxCompositeELResolver elResolver = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces) {

            @Override
            public void setResourceBundleVars(Set<String> resourceBundleVars) {
                vars[0] = resourceBundleVars;
                super.setResourceBundleVars(resourceBundleVars);
            }

        };

        ELUtils.buildFacesResolver(elResolver, applicationAssociate);

        assertEquals(Set.of("msgs"), vars[0]);
    }

    @Test
    public void resourceBundleAddedAfterItsVarWasLookedUpIsResolved() {
        applicationAssociate.setExpressionFactory(new ExpressionFactoryImpl());
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getAttributes().put(RIConstants.CDI_BEAN_MANAGER, new MockBeanManager() {

            @Override
            public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
                return Set.of();
            }

            @Override
            public Set<Bean<?>> getBeans(String name) {
                return Set.of();
            }

        });
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, ApplicationFactoryImpl.class.getName());
        ((ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY)).setApplication(applicationAssociate.getApplication());
        ELResolver elResolver = facesContext.getApplication().getELResolver();
        ELContext elContext = facesContext.getELContext();
        elResolver.getValue(elContext, null, "msgs");

        applicationAssociate.addResourceBundle("msgs", messages());
        elContext.setPropertyResolved(false);

        assertInstanceOf(Messages.class, elResolver.getValue(elContext, null, "msgs"));
    }

}
