package com.sun.faces.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.junit.JUnitFacesTestCaseBase;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import static com.sun.faces.RIConstants.FACES_PREFIX;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class ExternalContextImplWebsocketTest extends JUnitFacesTestCaseBase {

    public ExternalContextImplWebsocketTest() {
        super("ExternalContextImplWebsocketTest");
    }

    @Test
    public void testEncodeWebsocketURLOverHTTP() {
        ExternalContextImpl externalContext = prepareExternalContext("http://host/site.xhtml");

        assertEquals("ws://host/test", externalContext.encodeWebsocketURL("/test"));
    }

    @Test
    public void testEncodeWebsocketURLOverHTTPWithPort() {
        ExternalContextImpl externalContext = prepareExternalContext("http://host:1234/site.xhtml");

        assertEquals("ws://host:1234/test", externalContext.encodeWebsocketURL("/test"));
    }

    @Test
    public void testEncodeWebsocketURLOverHTTPS() {
        ExternalContextImpl externalContext = prepareExternalContext("https://host/site.xhtml");

        assertEquals("wss://host/test", externalContext.encodeWebsocketURL("/test"));
    }

    @Test
    public void testEncodeWebsocketURLOverHTTPSWithPort() {
        ExternalContextImpl externalContext = prepareExternalContext("https://host:1234/site.xhtml");

        assertEquals("wss://host:1234/test", externalContext.encodeWebsocketURL("/test"));
    }

    private ExternalContextImpl prepareExternalContext(String requestURL) {
        facesContext.getAttributes().put(FACES_PREFIX + "ExternalContextImpl.PUSH_SUPPORTED", Boolean.TRUE);

        ServletContext servletContext = PowerMock.createNiceMock(ServletContext.class);
        HttpServletRequest request = PowerMock.createNiceMock(HttpServletRequest.class);
        HttpServletResponse response = PowerMock.createNiceMock(HttpServletResponse.class);

        ApplicationAssociate applicationAssociate = PowerMock.createMock(ApplicationAssociate.class);
        expect(servletContext.getAttribute(RIConstants.FACES_PREFIX + "ApplicationAssociate")).andReturn(applicationAssociate);

        Capture<String> encodeCapture = new Capture<>(CaptureType.ALL);
        expect(response.encodeURL(capture(encodeCapture))).andAnswer(encodeCapture::getValue);
        expect(request.getRequestURL()).andReturn(new StringBuffer(requestURL));

        replay(servletContext, request, response);

        ExternalContextImpl externalContext = new ExternalContextImpl(servletContext, request, response);
        facesContext.setExternalContext(externalContext);

        return externalContext;
    }
}
