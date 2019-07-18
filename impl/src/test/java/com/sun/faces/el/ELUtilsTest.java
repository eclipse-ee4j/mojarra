package com.sun.faces.el;

import com.sun.el.ExpressionFactoryImpl;
import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationImpl;
import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.lifecycle.LifecycleImpl;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockServletContext;
import org.junit.Before;
import org.junit.Test;

import javax.el.ELResolver;
import javax.faces.FactoryFinder;
import java.net.URL;

public class ELUtilsTest {

    private ApplicationAssociate applicationAssociate;

    @Before
    public void setUp() {
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

        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY,
                "com.sun.faces.mock.MockRenderKitFactory");

        new FacesContextImpl(externalContext, new LifecycleImpl());
        new ApplicationImpl();

        applicationAssociate = (ApplicationAssociate) externalContext.getApplicationMap()
                .get(RIConstants.FACES_PREFIX + "ApplicationAssociate");
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
