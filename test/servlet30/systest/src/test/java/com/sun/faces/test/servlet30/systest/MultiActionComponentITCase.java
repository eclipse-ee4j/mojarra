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
import junit.framework.Test;
import junit.framework.TestSuite;

public class MultiActionComponentITCase extends HtmlUnitFacesITCase {

    public MultiActionComponentITCase(String name) {
        super(name);
    }

    public static Test suite() {
        return (new TestSuite(MultiActionComponentITCase.class));
    }

    public void test01() throws Exception {
        doTest("/faces/composite/compositeComponentWithMultipleActions.xhtml");

    }

    public void test02() throws Exception {
        doTest("/faces/composite/compositeComponentWithMultipleActionsMethodSignatures.xhtml");

    }

    public void doTest(String path) throws Exception {
        HtmlPage page = getPage(path);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("cc:submitAction");
        page = button.click();
        assertTrue("Expected submit pressed, received: " + page.asText(), page.asText().matches("(?s).*submit pressed.*"));

        page = getPage(path);
        button = (HtmlSubmitInput) page.getElementById("cc:cancelAction");
        page = button.click();
        assertTrue("Expected cancel pressed, received: " + page.asText(), page.asText().matches("(?s).*cancel pressed.*"));

    }
}
