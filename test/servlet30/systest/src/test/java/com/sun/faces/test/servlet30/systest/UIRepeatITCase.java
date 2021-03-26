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

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test cases for Facelets functionality
 */
public class UIRepeatITCase extends HtmlUnitFacesITCase {


    // --------------------------------------------------------------- Test Init


    public UIRepeatITCase() {
        this("UIRepeatTestCase");

        // this test is excluded because it won't pass in tomcat due to an issue with NumberConverter
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "testUIRepeatStateNotLostOnNonUIRepeatMessage");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "testUIRepeatStateNotLostOnNonUIRepeatMessage");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "testUIRepeatStateNotLostOnNonUIRepeatMessage");

    }


    public UIRepeatITCase(String name) {
        super(name);
    }


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
        return (new TestSuite(UIRepeatITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------------------ Test Methods


    /*
     * Added for issue 917.
     */
    public void testUIRepeat() throws Exception {

        HtmlPage page = getPage("/faces/facelets/uirepeat.xhtml") ;
        
        String text = page.asText();
        
        assertTrue(-1 != text.indexOf("ListFlavor is chocolate. Begin is . End is . Index is 0. Step is . Index is even: true. Index is odd: false. Index is first: true. Index is last: false."));
        assertTrue(-1 != text.indexOf("ListFlavor is vanilla. Begin is . End is . Index is 1. Step is . Index is even: false. Index is odd: true. Index is first: false. Index is last: false."));
        assertTrue(-1 != text.indexOf("ListFlavor is strawberry. Begin is . End is . Index is 2. Step is . Index is even: true. Index is odd: false. Index is first: false. Index is last: false."));
        assertTrue(-1 != text.indexOf("ListFlavor is chocolate peanut butter. Begin is . End is . Index is 3. Step is . Index is even: false. Index is odd: true. Index is first: false. Index is last: true."));
        assertTrue(-1 != text.indexOf("ArrayFlavor is chocolate. Begin is . End is . Index is 0. Step is . Index is even: true. Index is odd: false. Index is first: true. Index is last: false."));
        assertTrue(-1 != text.indexOf("ArrayFlavor is vanilla. Begin is . End is . Index is 1. Step is . Index is even: false. Index is odd: true. Index is first: false. Index is last: false."));
        assertTrue(-1 != text.indexOf("ArrayFlavor is strawberry. Begin is . End is . Index is 2. Step is . Index is even: true. Index is odd: false. Index is first: false. Index is last: false."));
        assertTrue(-1 != text.indexOf("ArrayFlavor is chocolate peanut butter. Begin is . End is . Index is 3. Step is . Index is even: false. Index is odd: true. Index is first: false. Index is last: true."));
        
    }


    public void testUIRepeatVarStatusBroadcast() throws Exception {

        HtmlPage page = getPage("/faces/facelets/uirepeat2.xhtml");
        List<HtmlAnchor> anchors = new ArrayList<HtmlAnchor>(4);
        getAllElementsOfGivenClass(page, anchors, HtmlAnchor.class);
        assertEquals("Expected to find only 4 HtmlAnchors", 4, anchors.size());
        
        String[] expectedValues = {
              "Index: 0",
              "Index: 1",
              "Index: 2",
              "Index: 3",
        };

        for (int i = 0, len = expectedValues.length; i < len; i++) {
            HtmlAnchor anchor = getAllElementsOfGivenClass(page, HtmlAnchor.class).get(i);
            
            page = anchor.click();
            
            assertTrue(
                "Page as text: \n\n" + page.asText()   +
                "\nDid not contain: \n " + expectedValues[i],
                page.asText().contains(expectedValues[i])
            );
        }

    }


    public void testUIRepeatStateNotLostOnNonUIRepeatMessage() throws Exception {

        HtmlPage page = getPage("/faces/facelets/uirepeat3.xhtml");
        List<HtmlTextInput> inputs = new ArrayList<HtmlTextInput>(5);
        getAllElementsOfGivenClass(page, inputs, HtmlTextInput.class);
        assertEquals("Expected 5 input fields", 5, inputs.size());
        inputs.get(0).setValueAttribute("A"); // this causes a validation failure
        inputs.get(1).setValueAttribute("1");
        inputs.get(2).setValueAttribute("2");
        inputs.get(3).setValueAttribute("3");
        inputs.get(4).setValueAttribute("4");
        HtmlSubmitInput submit = (HtmlSubmitInput) getInputContainingGivenId(page, "submit");
        page = submit.click();
        assertTrue(page.asText().contains("'A' is not a number."));
        // now verify the inputs nested within the UIRepeat were not cleared
        inputs.clear();
        getAllElementsOfGivenClass(page, inputs, HtmlTextInput.class);
        assertEquals("A", inputs.get(0).getValueAttribute());
        assertEquals("1", inputs.get(1).getValueAttribute());
        assertEquals("2", inputs.get(2).getValueAttribute());
        assertEquals("3", inputs.get(3).getValueAttribute());
        assertEquals("4", inputs.get(4).getValueAttribute());

    }


    public void testUIRepeatVarBeginEndStepProperties() throws Exception {

        HtmlPage page = getPage("/faces/facelets/uirepeat4.xhtml");
        List<HtmlSpan> spans = new ArrayList<HtmlSpan>(9);
        getAllElementsOfGivenClass(page, spans, HtmlSpan.class);
        assertEquals("Expected 9 spans", 9, spans.size());
        String[] expectedValues = {
              "vanilla : index=1 : begin=1 : end= : step= : first=true : last=false : even=true : odd=false",
              "strawberry : index=2 : begin=1 : end= : step= : first=false : last=false : even=false : odd=true",
              "chocolate peanut butter : index=3 : begin=1 : end= : step= : first=false : last=true : even=true : odd=false",
              "strawberry: index=2 : begin=2 : end=3 : step= : first=true : last=false : even=true : odd=false",
              "chocolate peanut butter: index=3 : begin=2 : end=3 : step= : first=false : last=true : even=false : odd=true",
              "chocolate: index=0 : begin= : end= : step=2 : first=true : last=false : even=true : odd=false",
              "strawberry: index=2 : begin= : end= : step=2 : first=false : last=true : even=false : odd=true",
              "vanilla: index=1 : begin=1 : end=1 : step=2 : first=true : last=true : even=true : odd=false",
              "chocolate: index=0 : begin= : end= : step= : first=true : last=true : even=true : odd=false"
        };
        for (int i = 0, len = spans.size(); i < len; i++) {
            assertEquals("Expected: " + expectedValues[i] + ", received: " + spans.get(i).asText(),
                         expectedValues[i],
                         spans.get(i).asText());
        }

    }

    /**
     * Added for issue 1218.
     */
    public void testForEachVarStatusNoException() throws Exception {

        HtmlPage page = getPage("/faces/facelets/forEach.xhtml");
        assertTrue(page.asText().contains("1 2 3"));

    }

    /******* PENDING(edburns): disable this test until JAVASERVERFACES-2356 is resolved
    public void testDebugViewState() throws Exception {
        tearDown();
        setUp();
        HtmlPage page = getPage("/faces/facelets/uirepeat5.xhtml");
        HtmlElement form = page.getElementById("form");
        page = (HtmlPage) form.type('D', true, true, false);
        List<WebWindow> windows = client.getWebWindows();
        WebWindow debugWindow = windows.get(1);
        page = (HtmlPage) debugWindow.getEnclosedPage();
        String xml = page.asXml();
        assertTrue(xml.matches("(?s).*<th>\\s*Total\\s*</th>\\s*<th>\\s*[0-9]*\\s*</th>.*"));


    }
    ***********/

}
