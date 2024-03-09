package com.sun.faces.el;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.el.ExpressionFactoryImpl;
import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationImpl;
import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.lifecycle.LifecycleImpl;
import com.sun.faces.mock.MockBeanManager;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockServletContext;

import jakarta.el.ELResolver;
import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;

public class ELUtilsTest {

    private ApplicationAssociate applicationAssociate;

    @BeforeEach
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

        FacesContext.getCurrentInstance().getAttributes().put(RIConstants.CDI_BEAN_MANAGER, new MockBeanManager());
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
