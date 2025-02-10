package com.sun.faces.application;

import com.sun.faces.config.InitFacesContext;
import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockServletContext;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;

public class WebappLifecycleListenerTestCase extends JUnitFacesTestCaseBase {

    private final static String SYNTHETIC_EXCEPTION_MESSAGE = "Synthetic exception";

    public WebappLifecycleListenerTestCase(String name) {
        super(name);
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(WebappLifecycleListenerTestCase.class));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        // This closes the MockFacesContext created by this test setUp.
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().release();
        }

        // This removes the InitFacesContext initialized in lifecycleListener.requestDestroyed(event) exception handling
        // logic. Not removing it would affect following tests.
        if (FacesContext.getCurrentInstance() instanceof InitFacesContext) {
            ((InitFacesContext) FacesContext.getCurrentInstance()).removeInitContextEntryForCurrentThread();
        }
    }

    /**
     * Tests that exception handling in WebappLifecycleListener.requestDestroyed(event) works.
     */
    public void testRequestDestroyedExceptionHandling() {
        // Create lifecycle listener and related objects.
        WebappLifecycleListener lifecycleListener = new WebappLifecycleListener(null);

        // Create a request event. Make it cause an exception inside the lifecycleListener.requestDestroyed(event) call.
        ServletRequestEvent event = new ServletRequestEvent(new MockServletContext(), new MockHttpServletRequest()) {
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
            Assert.fail("We expect a FacesException to be thrown from lifecycleListener.requestDestroyed(event)");
        } catch (FacesException e) {
            // FacesException has been produced as expected. The exception message should be the original synthetic
            // exception message.
            Assert.assertEquals(SYNTHETIC_EXCEPTION_MESSAGE, e.getMessage());
        }
    }
}
