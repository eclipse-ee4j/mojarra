/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation.
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

package com.sun.faces.test.servlet50.inputtext;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(Arquillian.class)
public class Spec1560IT {

    @ArquillianResource
    private URL webUrl;
    private WebClient webClient;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return create(ZipImporter.class, getProperty("finalName") + ".war")
                .importFrom(new File("target/" + getProperty("finalName") + ".war"))
                .as(WebArchive.class);
    }

    @Before
    public void setUp() {
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void test() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1560IT.xhtml");

        HtmlInput noType = (HtmlInput) page.getElementById("form:noType");
        assertEquals("Default type is 'text'", "text", noType.getTypeAttribute());

        HtmlInput typeEmail = (HtmlInput) page.getElementById("form:typeEmail");
        assertEquals("Type set via actual attribute is 'email'", "email", typeEmail.getTypeAttribute());

        HtmlInput passthroughTypeEmail = (HtmlInput) page.getElementById("form:passthroughTypeEmail");
        assertEquals("Type set via passthrough attribute is 'email'", "email", passthroughTypeEmail.getTypeAttribute());

        HtmlInput typeTelAndPassthroughTypeEmail = (HtmlInput) page.getElementById("form:typeTelAndPassthroughTypeEmail");
        assertEquals("Type overridden via passthrough attribute is 'email'", "email", typeTelAndPassthroughTypeEmail.getTypeAttribute());

        HtmlInput typeButton = (HtmlInput) page.getElementById("form:typeButton");
        assertEquals("Type set via actual attribute is 'button'", "button", typeButton.getTypeAttribute());

        HtmlElement messageForTypeEmail = (HtmlElement) page.getElementById("form:messageForTypeEmail");
        HtmlElement messageForTypeButton = (HtmlElement) page.getElementById("form:messageForTypeButton");
        HtmlElement messages = (HtmlElement) page.getElementById("messages");

        String emailMessage = messageForTypeEmail.asNormalizedText();
        String buttonMessage = messageForTypeButton.asNormalizedText();
        String globalMessage = messages.asNormalizedText();

        if ("Development".equals(System.getProperty("webapp.projectStage"))) {
            assertEquals("There is no faces message for type 'email'", "", emailMessage);
            assertNotEquals("There is a faces message for type 'button'", "", buttonMessage);
            assertEquals("The message for type 'button' is the only message set", globalMessage, buttonMessage);
        }
        else {
            assertEquals("There is no faces message for type 'email'", "", emailMessage);
            assertEquals("There is no faces message for type 'button'", "", buttonMessage);
            assertEquals("There is no faces message set at all", "", globalMessage);
        }
    }

}
