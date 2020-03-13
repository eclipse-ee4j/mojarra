/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet31.faceletsID;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>Simple test to ensure that context param <em>com.sun.faces.useFaceletsID</em>
 * really disables the IdMapper and IDs are generated using the facelet ID.</p>
 *
 * @author rmartinc
 */
public class FaceletIDITCase {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testUsingFaceletsID() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        DomNodeList<DomElement> nodes = page.getElementsByTagName("form");

        Assert.assertEquals(1, nodes.size());
        HtmlForm form = (HtmlForm) nodes.get(0);

        Assert.assertThat("ID is not using IdMapper", form.getId(),
                CoreMatchers.not(CoreMatchers.startsWith("j_idt")));
        Assert.assertThat("Name is not using IdMapper", form.getAttribute("name"),
                CoreMatchers.not(CoreMatchers.startsWith("j_idt")));
    }
}
