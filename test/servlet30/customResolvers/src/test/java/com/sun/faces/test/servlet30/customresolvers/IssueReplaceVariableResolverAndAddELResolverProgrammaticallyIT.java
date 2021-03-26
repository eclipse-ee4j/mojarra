/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.customresolvers;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>
 * Make sure that an application that replaces the ApplicationFactory but uses
 * the decorator pattern to allow the existing ApplicationImpl to do the bulk of
 * the requests works.</p>
 */
public class IssueReplaceVariableResolverAndAddELResolverProgrammaticallyIT {

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
    public void testReplaceVariableResolverAndAddELResolverProgrammatically() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/test.jsp");
        assertTrue(-1 != page.asText().indexOf("Invoking the variable resolver chain: success."));
        assertTrue(-1 != page.asText().indexOf("Invoking the variable resolver directly: success."));
        assertTrue(-1 != page.asText().indexOf("Invoking the EL resolver directly: true."));
        assertTrue(-1 != page.asText().indexOf("result: isReadOnly invoked directly."));
        assertTrue(-1 != page.asText().indexOf("Invoking the EL resolver via chain: true."));
        assertTrue(-1 != page.asText().indexOf("result: isReadOnly invoked thru chain."));
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("reload");
        page = (HtmlPage) button.click();
        String text = page.asXml();
        text = text.replaceAll(":[0-9]*\\)", "\\)");
        text = text.replaceAll("com.sun.faces.", "");
        text = text.replaceAll("toString() invocation", "");

        String[] orderedListOfStringsToFindInPage = {
            "FacesELResolverForFaces",
            "el.ImplicitObjectELResolver.getValue",
            "el.VariableResolverChainWrapper.getValue",
            "NewVariableResolver.resolveVariable",
            "NewELResolver.getValue",
            "el.ManagedBeanELResolver.resolveBean",
            "el.FacesResourceBundleELResolver.getValue",
            "el.ScopedAttributeELResolver.getValue",
            "FacesELResolverForJsp",
            "NewVariableResolver.resolveVariable",
            "NewELResolver.getValue"
        };
        boolean[] foundFlags = new boolean[orderedListOfStringsToFindInPage.length];
        int i, j;
        for (i = 0; i < foundFlags.length; i++) {
            foundFlags[i] = false;
        }
        String[] textSplitOnSpace = text.split(" ");
        j = 0;
        for (i = 0; i < textSplitOnSpace.length
                && j < orderedListOfStringsToFindInPage.length; i++) {
            if (textSplitOnSpace[i].contains(orderedListOfStringsToFindInPage[j])) {
                foundFlags[j++] = true;
            }
        }
        for (i = 0; i < foundFlags.length; i++) {
            if (!foundFlags[i]) {
                fail("Unable to find " + orderedListOfStringsToFindInPage[i]
                        + " at expected order in ELResolver chain.  Text: " + text);
            }
        }
    }

}
