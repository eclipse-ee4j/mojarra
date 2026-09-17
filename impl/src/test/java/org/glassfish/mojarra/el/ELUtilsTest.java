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

import java.lang.reflect.Method;
import java.net.URL;

import jakarta.el.ELResolver;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;

import org.glassfish.mojarra.RIConstants;
import org.glassfish.mojarra.application.ApplicationAssociate;
import org.glassfish.mojarra.application.ApplicationImpl;
import org.glassfish.mojarra.context.ExternalContextImpl;
import org.glassfish.mojarra.context.FacesContextImpl;
import org.glassfish.mojarra.lifecycle.LifecycleImpl;
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

}
