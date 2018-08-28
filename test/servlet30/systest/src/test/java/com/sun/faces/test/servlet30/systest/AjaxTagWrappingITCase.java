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

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AjaxTagWrappingITCase extends HtmlUnitFacesITCase {

    public AjaxTagWrappingITCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return new TestSuite(AjaxTagWrappingITCase.class);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    /*
     * Test each component to see that it behaves correctly when used with an Ajax tag
     */
    public void testAjaxTagWrapping() throws Exception {
        getPage("/faces/ajax/ajaxTagWrapping.xhtml");
        System.out.println("Start ajax tag wrapping test");

        // First we'll check the first page was output correctly
        checkTrue("out1", "0");
        checkTrue("checkedvalue", "false");
        checkTrue("outtext", "");

        // Submit the ajax request
        HtmlSubmitInput button1 = (HtmlSubmitInput) lastpage.getHtmlElementById("button1");
        lastpage = (HtmlPage) button1.click();

        // Check that the ajax request succeeds
        checkTrue("out1", "1");

        HtmlAnchor link1 = (HtmlAnchor) lastpage.getHtmlElementById("link1");
        lastpage = (HtmlPage) link1.click();

        // Check that the ajax request succeeds
        checkTrue("out1", "2");

        // Submit the ajax request
        HtmlSubmitInput button2 = (HtmlSubmitInput) lastpage.getHtmlElementById("button2");
        lastpage = (HtmlPage) button2.click();

        // Check that the ajax request succeeds
        checkTrue("out1", "3");

        // Check on the text field
        HtmlTextInput intext = ((HtmlTextInput) lastpage.getHtmlElementById("intext"));
        intext.focus();
        intext.type("test");
        intext.blur();

        checkTrue("outtext", "test");

        // Check on the text field
        HtmlTextInput intext2 = ((HtmlTextInput) lastpage.getHtmlElementById("intext2"));
        intext2.focus();
        intext2.type("test2");
        intext2.blur();

        checkTrue("outtext", "test2");

        // Check on the checkbox
        HtmlCheckBoxInput checked = ((HtmlCheckBoxInput) lastpage.getHtmlElementById("checkbox"));
        lastpage = (HtmlPage) checked.click();

        checkTrue("checkedvalue", "true");

        // Check on the select many checkbox
        checked = ((HtmlCheckBoxInput) lastpage.getHtmlElementById("manyCheckbox:0"));
        lastpage = (HtmlPage) checked.click();

        checkTrue("manyCheckedValue", "Value: 1");

    }

    public void testReturnFalseOnlyGeneratedOnAjaxInsideActionSourceComponents() throws Exception {
        HtmlPage page = getPage("/faces/ajax/issue1760NestedAjaxCheckboxRender.xhtml");

        sampleClickSample(page, "form1CurrentTime:", "checkbox1");
        sampleClickSample(page, "form2CurrentTime:", "checkbox2");
        sampleClickSample(page, "form3CurrentTime:", "button1");
        sampleClickSample(page, "form4CurrentTime:", "link1");

    }

    private void sampleClickSample(HtmlPage page, String timestampPrefix, String clickableElementId) throws Exception {
        String xml = page.asXml();
        verifyContent(xml);

        String timestampBefore = sampleTimestamp(timestampPrefix, xml);
        DomElement clickable = page.getElementById(clickableElementId);
        page = clickable.click();
        client.waitForBackgroundJavaScript(1000);

        xml = page.asXml();
        verifyContent(xml);
        String timestampAfter = sampleTimestamp(timestampPrefix, xml);

        assertTrue(!timestampBefore.equals(timestampAfter));
    }

    private String sampleTimestamp(String timestampPrefix, String xml) {
        String result = null;
        int prefixLen = timestampPrefix.length();
        int i = xml.indexOf(timestampPrefix);
        int j = xml.indexOf(".", i);
        result = xml.substring(i + prefixLen, j);

        return result;
    }

    private void verifyContent(String xml) throws Exception {
        // Verify that return false is *not* present on ajax request for form1
        // and form2.
        assertTrue(xml.matches("(?s).*<form id=\"form1\".*<input id=\"checkbox1\".*onclick=\".*[^f][^a][^l][^s][^e].*</form.*"));
        assertTrue(xml.matches("(?s).*<form id=\"form2\".*<input id=\"checkbox2\".*onclick=\".*[^f][^a][^l][^s][^e].*</form.*"));

        // Verify that return false *is* present on the ajax request for form3
        // and form4
        assertTrue(xml.matches("(?s).*<form id=\"form3\".*<input.*type=\"submit\".*onclick=\".*return false\".*</form.*"));
        assertTrue(xml.matches("(?s).*<form id=\"form3\".*<a.*onclick=\".*return false\".*</form.*"));

    }

}
