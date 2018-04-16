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

// TestRenderResponsePhase.java

package com.sun.faces.lifecycle;

import com.sun.faces.cactus.FileOutputResponseWrapper;
import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.util.Util;
import org.apache.cactus.WebRequest;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import java.io.File;
import java.util.Locale;

/**
 * <B>TestRenderResponsePhase</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestRenderResponsePhase extends JspFacesTestCase {

//
// Protected Constants
//

    public static final String TEST_URI = "/TestRenderResponsePhase.jsp";


    public String getExpectedOutputFilename() {
        return "RenderResponse_correct";
    }


    public static final String ignore[] = {
    };


    public String[] getLinesToIgnore() {
        return ignore;
    }


    public boolean sendResponseToFile() {
        return true;
    }


//
// Class Variables
//

//
// Instance Variables
//

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public TestRenderResponsePhase() {
        super("TestRenderResponsePhase");
	initLocalHostPath();
    }


    public TestRenderResponsePhase(String name) {
        super(name);
	initLocalHostPath();
    }

    private String localHostPath = "localhost:8080";

    private void initLocalHostPath() {
	String containerPort = System.getProperty("container.port");
	if (null == containerPort || 0 == containerPort.length()) {
	    containerPort = "8080";
	}
	localHostPath = "localhost:" + containerPort;
    }

//
// Class methods
//

//
// General Methods
//


    public void beginHtmlBasicRenderKit(WebRequest theRequest) {
        theRequest.setURL(localHostPath, "/test", "/faces", TEST_URI, null);
    }


    public void testHtmlBasicRenderKit() {

        Phase renderResponse = new RenderResponsePhase();
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        page.setId("root");
        page.setViewId(TEST_URI);
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);

        try {
            renderResponse.execute(getFacesContext());
        } catch (FacesException fe) {
            System.out.println(fe.getMessage());
            if (null != fe.getCause()) {
                fe.getCause().printStackTrace();
            } else {
                fe.printStackTrace();
            }
        }
        assertTrue(!(getFacesContext().getRenderResponse()) &&
                !(getFacesContext().getResponseComplete()));

       assertTrue(verifyExpectedOutput());
    }

    public void beginShortCircuitRenderResponse(WebRequest theRequest) {
        theRequest.setURL(localHostPath, "/test", "/faces", TEST_URI, null);
    }

    public void testShortCircuitRenderResponse() {

        SystemEventListener listener = new TestListener(getFacesContext());
        Application application = getFacesContext().getApplication();
        application.subscribeToEvent(PreRenderViewEvent.class, listener);

        Phase renderResponse = new RenderResponsePhase();
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        page.setId("root");
        page.setViewId(TEST_URI);
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);

        try {
            renderResponse.execute(getFacesContext());
        } catch (FacesException fe) {
            System.out.println(fe.getMessage());
            if (null != fe.getCause()) {
                fe.getCause().printStackTrace();
            } else {
                fe.printStackTrace();
            }
        }

        assertTrue(getFacesContext().getResponseComplete());
        File renderedOutputFile = new File(FileOutputResponseWrapper.FACES_RESPONSE_FILENAME);
        assertTrue(renderedOutputFile.length() == 0);
    }

    private static final class TestListener
        implements SystemEventListener {

        private FacesContext context = null;

        private Class<?> sourceFor;
        private Object passedSource;
        private boolean forSourceInvoked;


        public TestListener(FacesContext context) {
            this.context = context;
        }

        public void processEvent(SystemEvent event)
            throws AbortProcessingException {
            context.responseComplete();
        }

        public boolean isListenerForSource(Object source) {
            forSourceInvoked = true;
            passedSource = source;
            if (sourceFor == null) {
                return (source != null);
            } else {
                return sourceFor.isInstance(source);
            }
        }

    }

} // end of class TestRenderResponsePhase
