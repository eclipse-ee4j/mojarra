package org.glassfish.mojarra.el;

import java.net.URL;

import jakarta.el.ELResolver;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FactoryFinder;

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
