/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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


import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;

public class Jsf2JspITCase extends HtmlUnitFacesITCase {


    public Jsf2JspITCase() {
        this("Jsf2Js2TestCase");
    }


    public Jsf2JspITCase(String name) {
        super(name);
    }

    public static Test suite() {
        return (new TestSuite(Jsf2JspITCase.class));
    }


    // ------------------------------------------------------------ Test Methods


    public void testUnsupportedFeaturesAreUnsupported() throws Exception {

        // These features are not implemented in JSP
        assert500Response("/faces/jsf2jsp/head-gives-500.jspx");
        assert500Response("/faces/jsf2jsp/body-gives-500.jspx");
        assert500Response("/faces/jsf2jsp/outputScript-gives-500.jspx");
        assert500Response("/faces/jsf2jsp/outputStylesheet-gives-500.jspx");
        assert500Response("/faces/jsf2jsp/button-gives-500.jspx");
        assert500Response("/faces/jsf2jsp/link-gives-500.jspx");
        assert500Response("/faces/jsf2jsp/resource-ELResolver-gives-500.jspx");
        assert500Response("/faces/jsf2jsp/ajax-gives-500.jspx");
        assert500Response("/faces/jsf2jsp/event-gives-500.jspx");
        assert500Response("/faces/jsf2jsp/metadata-gives-500.jspx");

    }

    public void testSupportedFeaturesAreSupported() throws Exception {

//        // These features are implemented in JSP
//        HtmlPage page = getPage("/faces/jsf2jsp/commandButton-parameter-children-gives-hidden-fields.jspx");
//        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("reload");
//        page = button.click();
//        String text = page.asText();
//        assertTrue(text.contains("name01=value01"));
//        assertTrue(text.contains("name02=value02"));
//
//
//        page = getPage("/faces/jsf2jsp/resources.jspx");
//        text = page.asXml();
//        assertTrue(text.contains("duke.gif"));
//        assertTrue(text.contains("vLibrary"));
//        assertTrue(text.contains("2_01_1"));
//
//        assert200Response("/faces/jsf2jsp/selectManyJsf2Features.jspx");
//
//        Test validatorTest = ValidatorITCase.suite();
//        TestResult validatorResult = new TestResult();
//        validatorTest.run(validatorResult);
//        assertTrue(validatorResult.failureCount() == 0);
//
//
//
    }
    
    private void assert500Response(String urlFragment) throws Exception {
        client.getOptions().setThrowExceptionOnFailingStatusCode(true);
        HtmlPage page = null;
        int code;
        try {
            page = getPage(urlFragment);
        } catch (FailingHttpStatusCodeException fail) {
            code = fail.getStatusCode();
            assertTrue("GET " + urlFragment + " Expected 500, got: "+code, code==500);
        }
        
    }

    private void assert200Response(String urlFragment) throws Exception {
        client.getOptions().setThrowExceptionOnFailingStatusCode(true);
        HtmlPage page = null;
        int code;
        try {
            page = getPage(urlFragment);
        } catch (FailingHttpStatusCodeException fail) {
            code = fail.getStatusCode();
            assertTrue("GET " + urlFragment + " Expected 200, got: "+code, code==200);
        }

    }

}
