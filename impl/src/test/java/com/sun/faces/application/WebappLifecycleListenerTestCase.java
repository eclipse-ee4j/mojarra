package com.sun.faces.application;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableDistributable;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockHttpServletRequest;


public class WebappLifecycleListenerTestCase extends JUnitFacesTestCaseBase {

    private final static String SYNTHETIC_EXCEPTION_MESSAGE = "Synthetic exception";

    @AfterEach
    public void tearDown() throws Exception {
        // The InitFacesContext initialized during this test tends to stay around, affecting subsequent tests. Following
        // steps are done to get rid of it.

        // This closes the MockFacesContext created in this test setUp method.
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().release();
        }

        // To release ServletContextFacesContextFactory, which also keeps reference to InitFacesContext instance.
        FactoryFinder.releaseFactories();
    }
    /**
     * Tests that exception handling in WebappLifecycleListener.requestDestroyed(event) works.
     */
    @Test
    public void testRequestDestroyedExceptionHandling() {
        // Create lifecycle listener and related objects.
        WebappLifecycleListener lifecycleListener = new WebappLifecycleListener(null);

        WebConfiguration webConfiguration = WebConfiguration.getInstance(servletContext);
        webConfiguration.setOptionEnabled(EnableDistributable, true);

        // Create a request event. Make it cause an exception inside the lifecycleListener.requestDestroyed(event) call.
        ServletRequestEvent event = new ServletRequestEvent(servletContext, new MockHttpServletRequest()) {
            @Override
            public ServletRequest getServletRequest() {
                // This is just a convenient place to throw the exception for testing purposes. In the real impl the
                // exception would come from a surrounding code, not from this particular method.
                throw new RuntimeException(SYNTHETIC_EXCEPTION_MESSAGE);
            }
        };

        try {
            // Call the requestDestroyed() lifecycle method. It should handle the synthetic exception thrown from
            // event.getServletRequest() and rethrow it as FacesException.
            // Certainly, we don't expect an UnsupportedOperationException from FacesContext.getExceptionHandler()
            // instead.
            lifecycleListener.requestDestroyed(event);
            Assertions.fail("We expect a FacesException to be thrown from lifecycleListener.requestDestroyed(event)");
        } catch (FacesException e) {
            // FacesException has been produced as expected. The exception message should be the original synthetic
            // exception message.
            Assertions.assertEquals(SYNTHETIC_EXCEPTION_MESSAGE, e.getMessage());
        }
    }
}
