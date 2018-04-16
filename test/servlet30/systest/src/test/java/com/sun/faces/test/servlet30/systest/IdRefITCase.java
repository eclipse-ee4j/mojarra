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

import com.gargoylesoftware.htmlunit.html.*;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.*;

/**
 * <p>
 * Test id-ref values in <code>h:message</code> and <code>h:outputLabel</code>
 * tags, and their interaction with <code>c:forEach</code>.
 * </p>
 */

public class IdRefITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------
    // Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public IdRefITCase(String name) {
        super(name);
        addExclusion(Container.TOMCAT6, "testIdRefs");
        addExclusion(Container.TOMCAT7, "testIdRefs");
        addExclusion(Container.WLS_10_3_4_NO_CLUSTER, "testIdRefs");
        addExclusion(Container.TOMCAT6, "testIncludedLoopIdRefs");
        addExclusion(Container.TOMCAT7, "testIncludedLoopIdRefs");
        addExclusion(Container.WLS_10_3_4_NO_CLUSTER, "testIncludedLoopIdRefs");
    }

    // ----------------------------------------------------
    // Overall Test Methods

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(IdRefITCase.class));
    }

    private Map mapElementsByAttribute(HtmlElement docElem, String tagName,
                                       String attName, String filterAtt, String filterValue) {
        Map elems = new TreeMap();
        List tags = docElem.getElementsByTagName(tagName);
        for (Iterator tagIt = tags.iterator(); tagIt.hasNext();) {
            HtmlElement tag = (HtmlElement) tagIt.next();
            if (filterAtt != null && filterValue != null &&
                    !filterValue.equals(tag.getAttribute(filterAtt))) {
                continue;
            }
            String attValue = tag.getAttribute(attName);
            assertNotNull(attName + " attribute of " + tagName, attValue);
            assertNotSame(attName + " attribute of " + tagName, 0, attValue.length());
            assertFalse("More than one " + tagName + " contains " + attName
                    + "=" + attValue, elems.containsKey(attValue));
            elems.put(attValue, tag);
        }
        return elems;
    }

    private Map mapMessagesById(HtmlElement docElem) {
        Map elems = new TreeMap();
        List tags = docElem.getElementsByTagName("span");
        for (Iterator tagIt = tags.iterator(); tagIt.hasNext();) {
            HtmlSpan tag = (HtmlSpan) tagIt.next();
            if ("message".equals(tag.getAttribute("class"))) {
                String text = tag.asText();
                assertNotSame(
                        "expect validation message to start with component id",
                        -1, text.indexOf(": "));
                String id = text.substring(0, text.indexOf(": ")).trim();
                assertFalse("Duplicate message for input " + id, elems
                        .containsKey(id));
                elems.put(id, tag);
            }
        }
        return elems;
    }

    // -------------------------------------------------
    // Individual Test Methods
    public void testIdRefs() throws Exception {
//        HtmlPage page = getPage("/faces/forEach03.jsp");
//
//        // assert every input has a label, and every label refers to an input
//        Map inputTagsById = mapElementsByAttribute(page.getDocumentElement(),
//                "input", "id", "type", "text");
//        Map labelTagsByFor = mapElementsByAttribute(page.getDocumentElement(),
//                "label", "for", null, null);
//        assertEquals("//label/@for set should be the same as //input/@id set",
//                inputTagsById.keySet(), labelTagsByFor.keySet());
//
//        // assign new values to input fields, submit the form.
//        String idPrefix = "myform:input";
//        String[] testIds = new String[]{idPrefix + "Int1", idPrefix + "Id1",
//                idPrefix + "Id2j_id_1", idPrefix + "Id3j_id_2"};
//        for (int i = 0; i < testIds.length; ++i) {
//            HtmlTextInput input = (HtmlTextInput) inputTagsById.get(testIds[i]);
//            input.setValueAttribute("");
//        }
//        List list = getAllElementsOfGivenClass(page, null,
//                HtmlSubmitInput.class);
//        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
//        page = (HtmlPage) button.click();
//
//        // make sure every 'value required' validation is present on post back.
//        Map messageMap = mapMessagesById(page.getDocumentElement());
//        assertEquals("One 'value required' message for each cleared input",
//                testIds.length, messageMap.size());
//        assertTrue("Only cleared inputs have messages", Arrays.asList(testIds)
//                .containsAll(messageMap.keySet()));
//        assertTrue("All cleared inputs have messages", messageMap.keySet()
//                .containsAll(Arrays.asList(testIds)));
    }

    public void testIncludedLoopIdRefs() throws Exception {
//        HtmlPage page = getPage("/faces/forEach03.jsp");
//        Map inputTagsById = mapElementsByAttribute(page.getDocumentElement(),
//                "input", "id", "type", "text");
//        String[] testIds = {
//                "myform:inputId11",
//                "myform:inputId11j_id_1",
//                "myform:inputId11j_id_2"
//        };
//        for (int i = 0; i < testIds.length; i++) {
//            HtmlTextInput input = (HtmlTextInput) inputTagsById.get(testIds[i]);
//            input.setValueAttribute("");
//        }
//        List list = getAllElementsOfGivenClass(page, null,
//                HtmlSubmitInput.class);
//        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
//        page = (HtmlPage) button.click();
//        Map messageMap = mapMessagesById(page.getDocumentElement());
//        assertEquals("One 'value required' message for each cleared input",
//                testIds.length, messageMap.size());
//        assertTrue("Only cleared inputs have messages", Arrays.asList(testIds)
//                .containsAll(messageMap.keySet()));
//        assertTrue("All cleared inputs have messages", messageMap.keySet()
//                .containsAll(Arrays.asList(testIds)));
    }

    public void testIncludeNoLoopIdRef() throws Exception {
//        HtmlPage page = getPage("/faces/forEach03.jsp");
//        Map inputTagsById = mapElementsByAttribute(page.getDocumentElement(),
//                "input", "id", "type", "text");
//        String[] testIds = {
//                "myform:Short11",
//        };
//        for (int i = 0; i < testIds.length; i++) {
//            HtmlTextInput input = (HtmlTextInput) inputTagsById.get(testIds[i]);
//            input.setValueAttribute("");
//        }
//        List list = getAllElementsOfGivenClass(page, null,
//                HtmlSubmitInput.class);
//        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
//        page = (HtmlPage) button.click();
//        Map messageMap = mapMessagesById(page.getDocumentElement());
//        assertEquals("One 'value required' message for each cleared input",
//                testIds.length, messageMap.size());
//        assertTrue("Only cleared inputs have messages", Arrays.asList(testIds)
//                .containsAll(messageMap.keySet()));
//        assertTrue("All cleared inputs have messages", messageMap.keySet()
//                .containsAll(Arrays.asList(testIds)));
    }

}
