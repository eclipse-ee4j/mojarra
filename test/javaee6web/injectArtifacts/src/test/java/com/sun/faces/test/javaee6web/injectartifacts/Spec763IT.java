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

package com.sun.faces.test.javaee6web.injectartifacts;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class Spec763IT {

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
    public void testInjectArtifacts() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        assertTrue(page.asXml().matches("(?s).*.p.\\s+FacesConfigApplicationFactory:\\s+Injected\\s+from\\s+value\\s+specified\\s+in\\s+web.xml\\s+@PostConstruct\\s+called\\s+./p.\\s+.p.\\s+FacesConfigActionListener:\\s+Injected\\s+from\\s+value\\s+specified\\s+in\\s+web.xml\\s+@PostConstruct\\s+called\\s+./p.\\s+.p.\\s+FacesConfigNavigationHandler:\\s+Injected\\s+from\\s+value\\s+specified\\s+in\\s+web.xml\\s+@PostConstruct\\s+called\\s+./p.\\s+.p.\\s+FacesConfigViewHandler:\\s+Injected\\s+from\\s+value\\s+specified\\s+in\\s+web.xml\\s+@PostConstruct\\s+called\\s+./p.\\s+.p.\\s+FacesConfigStateManager:\\s+Injected\\s+from\\s+value\\s+specified\\s+in\\s+web.xml\\s+@PostConstruct\\s+called\\s+./p.\\s+.p.\\s+FacesConfigELResolver:\\s+Injected\\s+from\\s+value\\s+specified\\s+in\\s+web.xml\\s+@PostConstruct\\s+called\\s+./p.\\s+.p.\\s+FacesConfigResourceHandler:\\s+Injected\\s+from\\s+value\\s+specified\\s+in\\s+web.xml\\s+@PostConstruct\\s+called\\s+./p.\\s+.p.\\s+FacesConfigSystemEventListener:\\s+Injected\\s+from\\s+value\\s+specified\\s+in\\s+web.xml\\s+@PostConstruct\\s+called\\s+./p.\\s+.p.\\s+FacesConfigPhaseListener:\\s+Injected\\s+from\\s+value\\s+specified\\s+in\\s+web.xml\\s+@PostConstruct\\s+called\\s+./p..*"));
    }
}
