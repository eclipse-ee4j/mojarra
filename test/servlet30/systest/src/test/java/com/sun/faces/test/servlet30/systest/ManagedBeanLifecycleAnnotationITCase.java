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

package com.sun.faces.test.servlet30.systest;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import java.util.regex.Pattern;
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;

/**
 * <p>
 * Test Case for JSP Interoperability.</p>
 */
public class ManagedBeanLifecycleAnnotationITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ManagedBeanLifecycleAnnotationITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables
    // ---------------------------------------------------- Overall Test Methods
    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ManagedBeanLifecycleAnnotationITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods
    public void testRequestLifecycle() throws Exception {
        String text = null;
        HtmlPage page = getPage("/faces/managed08.jsp");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        text = page.asText();
        Pattern pattern = null;
        assertTrue(-1 != text.indexOf("requestBean PostConstruct: true"));
        assertTrue(-1 != text.indexOf("requestBean PreDestroy: false"));
        assertTrue(-1 != text.indexOf("sessionBean PostConstruct: true"));
        assertTrue(-1 != text.indexOf("sessionBean PreDestroy: false"));
        assertTrue(-1 != text.indexOf("applicationBean PostConstruct: true"));
        assertTrue(-1 != text.indexOf("applicationBean PreDestroy: false"));
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:reload");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:removeSessionBean");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*bean: sessionBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*bean: sessionBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:removeSessionBean2");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*bean: sessionBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*bean: sessionBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:removeApplicationBean");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*bean: applicationBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*bean: applicationBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:removeApplicationBean2");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*bean: applicationBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*bean: applicationBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:invalidateSession");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*bean: sessionBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*bean: sessionBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearSessionMapTwice");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*bean: sessionBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*bean: sessionBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:replaceRequestBean");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*-----------------.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:replaceRequestBean2");
        button.click();
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:removeSessionBean");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:replaceSessionBean");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*bean: sessionBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:removeSessionBean");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:replaceSessionBean2");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:removeApplicationBean");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:replaceApplicationBean");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*bean: applicationBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*",
                text));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:removeApplicationBean");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:clearStatusMessage");
        page = (HtmlPage) button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("form:replaceApplicationBean2");
        page = (HtmlPage) button.click();
        text = page.asText();
        assertTrue(Pattern.matches("(?s).*-----------------.*bean: requestBean postConstructCalled: true.*bean: requestBean preDestroyCalled: true.*-----------------.*bean: requestBean postConstructCalled: true.*",
                text));

    }

}
