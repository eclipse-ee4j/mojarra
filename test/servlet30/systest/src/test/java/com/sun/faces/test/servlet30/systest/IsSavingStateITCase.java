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

public class IsSavingStateITCase extends HtmlUnitFacesITCase {

    public IsSavingStateITCase(String name) {
        super(name);
    }

    public static Test suite() {
        return (new TestSuite(IsSavingStateITCase.class));
    }

    public void test01() throws Exception {

        HtmlPage page = getPage("/faces/state/isSavingState.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("next");
        page = button.click();
        assertTrue(page.asText().matches("(?s).*beforeMessage:.*no.*value.*duringMessage:.*true.*afterMessage:.*no.*value.*"));
        page = getPage("/faces/state/isSavingState2.xhtml");
        String text = page.asText();
        assertFalse(page.asText().matches("(?s).*beforeMessage:.*no.*value.*duringMessage:.*true.*afterMessage:.*no.*value.*"));
        assertTrue(page.asText().matches("(?s).*beforeMessage:.*duringMessage:.*afterMessage:.*"));

    }
}
