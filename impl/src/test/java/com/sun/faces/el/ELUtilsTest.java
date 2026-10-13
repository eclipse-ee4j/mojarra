package com.sun.faces.el;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ListResourceBundle;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.el.ExpressionFactoryImpl;
import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationFactoryImpl;
import com.sun.faces.application.ApplicationImpl;
import com.sun.faces.application.ApplicationResourceBundle;
import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.lifecycle.LifecycleImpl;
import com.sun.faces.mock.MockBeanManager;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockServletContext;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ApplicationFactory;
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
