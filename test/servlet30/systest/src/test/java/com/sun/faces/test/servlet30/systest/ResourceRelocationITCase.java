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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHead;
import com.gargoylesoftware.htmlunit.html.HtmlTitle;
import com.gargoylesoftware.htmlunit.html.HtmlScript;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Validate resource re-location of scripts and stylesheets
 */
public class ResourceRelocationITCase extends HtmlUnitFacesITCase {

    public ResourceRelocationITCase() {
        this("ResourceRelocationTestCase");
    }

    public ResourceRelocationITCase(String name) {
        super(name);
    }

    public static Test suite() {
        return (new TestSuite(ResourceRelocationITCase.class));
    }

    // ------------------------------------------------------------ Test Methods

    public void testResourceRelocation() throws Exception {

        resourceRelocationTest("/faces/resourcerelocation.xhtml", true);
        resourceRelocationTest("/faces/resourcerelocation2.xhtml", false);
        resourceRelocationTest("/faces/resourcerelocation3.xhtml", false);
        resourceRelocationTest("/faces/resourcerelocation4.xhtml", true);

    }

    // --------------------------------------------------------- Private Methods

    private void resourceRelocationTest(String urlfrag, boolean scriptfirst) throws Exception {

        int scriptPos;
        int sheetPos;

        if (scriptfirst) {
            scriptPos = 1;
            sheetPos = 2;
        } else {
            sheetPos = 1;
            scriptPos = 2;
        }

        // for this request, the script and stylesheet will be in the head
        HtmlPage page = getPage(urlfrag + "?location=head");
        List<HtmlHead> headList = new ArrayList<HtmlHead>(1);
        getAllElementsOfGivenClass(page, headList, HtmlHead.class);
        assertTrue(headList.size() == 1);
        HtmlHead head = headList.get(0);
        List<HtmlElement> headChildren = getChildren(head);
        assertTrue(headChildren.size() == 3);
        assertTrue(headChildren.get(0) instanceof HtmlTitle);
        assertTrue(headChildren.get(scriptPos) instanceof HtmlScript);
        assertTrue(headChildren.get(sheetPos) instanceof HtmlLink);
        List<HtmlBody> bodyList = new ArrayList<HtmlBody>(1);
        getAllElementsOfGivenClass(page, bodyList, HtmlBody.class);
        assertTrue(bodyList.size() == 1);
        HtmlBody body = bodyList.get(0);
        List<HtmlElement> bodyChildren = getChildren(body);
        assertTrue(bodyChildren.size() == 1);
        assertTrue(bodyChildren.get(0) instanceof HtmlForm);
        List<HtmlForm> formList = new ArrayList<HtmlForm>(1);
        getAllElementsOfGivenClass(page, formList, HtmlForm.class);
        assertTrue(formList.size() == 1);
        HtmlForm form = formList.get(0);
        List<HtmlElement> formChildren = getChildren(form);
        assertTrue(formChildren.size() == 2);
        assertTrue(formChildren.get(0) instanceof HtmlInput);
        assertTrue(formChildren.get(1) instanceof HtmlInput);

        // for this request, the stylesheet will be in the head, and the script
        // will be the last child of body
        page = getPage(urlfrag + "?location=body");
        headList.clear();
        getAllElementsOfGivenClass(page, headList, HtmlHead.class);
        assertTrue(headList.size() == 1);
        head = headList.get(0);
        headChildren = getChildren(head);
        assertTrue(headChildren.size() == 2);
        assertTrue(headChildren.get(0) instanceof HtmlTitle);
        assertTrue(headChildren.get(1) instanceof HtmlLink);
        bodyList.clear();
        getAllElementsOfGivenClass(page, bodyList, HtmlBody.class);
        assertTrue(bodyList.size() == 1);
        body = bodyList.get(0);
        bodyChildren = getChildren(body);
        assertTrue(bodyChildren.size() == 2);
        assertTrue(bodyChildren.get(0) instanceof HtmlForm);
        assertTrue(bodyChildren.get(1) instanceof HtmlScript);
        formList.clear();
        getAllElementsOfGivenClass(page, formList, HtmlForm.class);
        assertTrue(formList.size() == 1);
        form = formList.get(0);
        formChildren = getChildren(form);
        assertTrue(formChildren.size() == 2);
        assertTrue(formChildren.get(0) instanceof HtmlInput);
        assertTrue(formChildren.get(1) instanceof HtmlInput);

        // for this request, the stylesheet will be in the head, and the
        // script will be the last child of the form
        page = getPage(urlfrag + "?location=form");
        headList.clear();
        getAllElementsOfGivenClass(page, headList, HtmlHead.class);
        assertTrue(headList.size() == 1);
        head = headList.get(0);
        headChildren = getChildren(head);
        assertTrue(headChildren.size() == 2);
        assertTrue(headChildren.get(0) instanceof HtmlTitle);
        assertTrue(headChildren.get(1) instanceof HtmlLink);
        bodyList.clear();
        getAllElementsOfGivenClass(page, bodyList, HtmlBody.class);
        assertTrue(bodyList.size() == 1);
        body = bodyList.get(0);
        bodyChildren = getChildren(body);
        assertTrue(bodyChildren.size() == 1);
        assertTrue(bodyChildren.get(0) instanceof HtmlForm);
        formList.clear();
        getAllElementsOfGivenClass(page, formList, HtmlForm.class);
        assertTrue(formList.size() == 1);
        form = formList.get(0);
        formChildren = getChildren(form);
        assertTrue(formChildren.size() == 3);
        assertTrue(formChildren.get(0) instanceof HtmlInput);
        assertTrue(formChildren.get(1) instanceof HtmlInput);
        assertTrue(formChildren.get(2) instanceof HtmlScript);

    }

    private List<HtmlElement> getChildren(HtmlElement parent) {
        List<HtmlElement> list = new ArrayList<HtmlElement>();
        for (Iterator i = parent.getChildElements().iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof HtmlElement) {
                list.add((HtmlElement) o);
            }
        }
        return list;
    }
}
