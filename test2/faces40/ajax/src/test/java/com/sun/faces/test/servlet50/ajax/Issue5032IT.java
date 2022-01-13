/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

package com.sun.faces.test.servlet50.ajax;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;

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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

@RunWith(Arquillian.class)
public class Issue5032IT {

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
    public void testImplicitThis() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "issue5032IT.xhtml");

        HtmlTextInput form1input2 = (HtmlTextInput) page.getElementById("form1:inputs:input2");
        assertEquals("f:ajax execute of form1:input2 is implied as @this", "mojarra.ab(this,event,'valueChange',0,'@form')", form1input2.getOnChangeAttribute());

        form1input2.setValueAttribute("1");
        form1input2.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        String form1messages = page.getElementById("form1:messages").asNormalizedText();
        assertEquals("there are no validation messages coming from required field form1:input1", "", form1messages);
    }

    @Test
    public void testExplicitThis() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "issue5032IT.xhtml");

        HtmlTextInput form2input2 = (HtmlTextInput) page.getElementById("form2:inputs:input2");
        assertEquals("f:ajax execute of form2:input2 is still @this", "mojarra.ab(this,event,'valueChange','@this','@form')", form2input2.getOnChangeAttribute());

        form2input2.setValueAttribute("1");
        form2input2.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        String form2messages = page.getElementById("form2:messages").asNormalizedText();
        assertEquals("there are no validation messages coming from required field form2:input1", "", form2messages);
    }

}
