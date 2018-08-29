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

import junit.framework.Test;
import junit.framework.TestSuite;

public class CompositeBehaviorITCase extends HtmlUnitFacesITCase {

    public CompositeBehaviorITCase(String name) {
        super(name);
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "test01");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "test01");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "test01");

    }

    public static Test suite() {
        return (new TestSuite(CompositeBehaviorITCase.class));
    }

    public void test01() throws Exception {

//        HtmlPage page = getPage("/faces/composite/behavior/composite.xhtml");
//        if (page.asXml().contains("Project Stage: Development")) {
//            HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:composite:cancel");
//            page = button.click();
//            assertTrue("Page does not contain validation message after clicking cancel button.",
//                    page.asXml().contains("Length"));
//            button = (HtmlSubmitInput) page.getElementById("form:composite:sub:commandAction");
//            page = button.click();
//            assertTrue("Page does not contain validation message after clicking ok with no text in textfield button.",
//                    page.asXml().contains("Length"));
//            button = (HtmlSubmitInput) page.getElementById("form:composite:sub:commandAction");
//            HtmlTextInput textField = (HtmlTextInput) page.getElementById("form:composite:input");
//            textField.setValueAttribute("more than three characters");
//            page = button.click();
//            assertTrue("Can't find the message: \"Reaching this page indicates that the method expression retargeting was successful.\"",
//                    page.asXml().contains("Reaching this page indicates that the method expression retargeting was successful."));
//        }
    }

}
