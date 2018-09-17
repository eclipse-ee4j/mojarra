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
import junit.framework.Test;
import junit.framework.TestSuite;

public class ConverterITCase extends HtmlUnitFacesITCase {

    public ConverterITCase(String name) {
        super(name);
    }

    public static Test suite() {
        return (new TestSuite(ConverterITCase.class));
    }

    public void test05() throws Exception {
        HtmlPage page = getPage("/faces/converter05.jsp");
        assertTrue(page.asXml().matches(
                "(?s).*<body>\\s*<span\\s*id=\\\"outputDatetime1\\\">\\s*7/10/96\\s*3:31:31\\s*PM\\s*EDT\\s*</span>\\s*<span\\s*id=\\\"outputDatetime2\\\">\\s*7/10/96\\s*3:31:31\\s*PM\\s*EDT\\s*</span>\\s*<span\\s*id=\\\"outputDatetime3\\\">\\s*7/10/96\\s*3:31:31\\s*PM\\s*EDT\\s*</span>\\s*<span\\s*id=\\\"outputNumber1\\\">\\s*10.000\\s*</span>\\s*<span\\s*id=\\\"outputNumber2\\\">\\s*10,000\\s*</span>\\s*<span\\s*id=\\\"outputNumber3\\\">\\s*10,000\\s*</span>\\s*</body>.*"));
    }
}
