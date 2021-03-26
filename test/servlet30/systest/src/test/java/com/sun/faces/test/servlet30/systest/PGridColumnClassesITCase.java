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



public class PGridColumnClassesITCase extends HtmlUnitFacesITCase {


    public PGridColumnClassesITCase(String name) {
        super(name);
    }

    public static Test suite() {
        return (new TestSuite(PGridColumnClassesITCase.class));
    }

    public void test01() throws Exception {
        HtmlPage page = getPage("/faces/standard/pgridcolumnclasses.jsp");
        String xml = page.asXml();
        String xmlWithoutWhitespace = xml.replaceAll("\\s{1,100}", "");
        assertTrue(xmlWithoutWhitespace.contains("<html><head><title>pgridcolumnclasses.jsp</title><styletype=\"text/css\">.b1{background-color:red;}.b2{background-color:green;}.b3{background-color:blue;}.b4{background-color:burlywood;}.b5{background-color:darkolivegreen;}.b6{background-color:darkviolet;}.b7{background-color:skyblue;}</style></head><body><table><tbody><tr><tdclass=\"b1\">c1</td><tdclass=\"b2\">c2</td><td>c3</td><td>c4</td><td>c5</td><td>c6</td></tr><tr><tdclass=\"b1\">c1_1</td><tdclass=\"b2\">c2_1</td><td>c3_1</td><td>c4_1</td><td>c5_1</td><td>c6_1</td></tr></tbody></table><table><tbody><tr><tdclass=\"b1\">c1</td><tdclass=\"b2\">c2</td><tdclass=\"b3\">c3</td><tdclass=\"b4\">c4</td><td>c5</td><td>c6</td></tr><tr><tdclass=\"b1\">c1_1</td><tdclass=\"b2\">c2_1</td><tdclass=\"b3\">c3_1</td><tdclass=\"b4\">c4_1</td><td>c5_1</td><td>c6_1</td></tr></tbody></table><table><tbody><tr><tdclass=\"b1\">c1</td><tdclass=\"b2\">c2</td><tdclass=\"b3\">c3</td><td>c4</td><td>c5</td><td>c6</td></tr><tr><tdclass=\"b1\">c1_1</td><tdclass=\"b2\">c2_1</td><tdclass=\"b3\">c3_1</td><td>c4_1</td><td>c5_1</td><td>c6_1</td></tr></tbody></table><table><tbody><tr><tdclass=\"b1\">c1</td><td>c2</td><td>c3</td><td>c4</td><td>c5</td><td>c6</td></tr><tr><tdclass=\"b1\">c1_1</td><td>c2_1</td><td>c3_1</td><td>c4_1</td><td>c5_1</td><td>c6_1</td></tr></tbody></table><table><tbody><tr><td>c1</td><td>c2</td><td>c3</td><td>c4</td><td>c5</td><td>c6</td></tr><tr><td>c1_1</td><td>c2_1</td><td>c3_1</td><td>c4_1</td><td>c5_1</td><td>c6_1</td></tr></tbody></table><table><tbody><tr><tdclass=\"b1\">c1</td><tdclass=\"b2\">c2</td><tdclass=\"b3\">c3</td><tdclass=\"b4\">c4</td><tdclass=\"b5\">c5</td><tdclass=\"b6\">c6</td></tr><tr><tdclass=\"b1\">c1_1</td><tdclass=\"b2\">c2_1</td><tdclass=\"b3\">c3_1</td><tdclass=\"b4\">c4_1</td><tdclass=\"b5\">c5_1</td><tdclass=\"b6\">c6_1</td></tr></tbody></table><table><tbody><tr><td>c1</td><tdclass=\"b2\">c2</td><td>c3</td><td>c4</td><tdclass=\"b5\">c5</td><tdclass=\"b6\">c6</td></tr><tr><td>c1_1</td><tdclass=\"b2\">c2_1</td><td>c3_1</td><td>c4_1</td><tdclass=\"b5\">c5_1</td><tdclass=\"b6\">c6_1</td></tr></tbody></table></body></html>"));
    }
}
