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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

@RunWith(Arquillian.class)
public class Spec1567IT {

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
        HtmlPage page = webClient.getPage(webUrl + "spec1567IT.xhtml");

        validateRenderedMarkup(page, "form1", "form1:messages");
        validateRenderedMarkup(page, "form2", "form2:messages");
        validateRenderedMarkup(page, "form3", "form3:inputs form3:messages");

        // fill form1 input1
        HtmlTextInput input1 = (HtmlTextInput) page.getElementById("form1:inputs:input1");
        input1.setValueAttribute("1");
        input1.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        input1 = (HtmlTextInput) page.getElementById("form1:inputs:input1");
        HtmlTextInput input2 = (HtmlTextInput) page.getElementById("form1:inputs:input2");
        HtmlTextInput input3 = (HtmlTextInput) page.getElementById("form1:inputs:input3");
        String messages = page.getElementById("form1:messages").asNormalizedText();
        assertEquals("input1 is filled with 1", "1", input1.getValueAttribute());
        assertEquals("input2 is empty", "", input2.getValueAttribute());
        assertEquals("input3 is empty", "", input3.getValueAttribute());
        assertEquals("input1 is filled and input2 is empty", "setForm1input1:1\nsetForm1input2:", messages);

        // fill form1 input2
        input2 = (HtmlTextInput) page.getElementById("form1:inputs:input2");
        input2.setValueAttribute("1");
        input2.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        input1 = (HtmlTextInput) page.getElementById("form1:inputs:input1");
        input2 = (HtmlTextInput) page.getElementById("form1:inputs:input2");
        input3 = (HtmlTextInput) page.getElementById("form1:inputs:input3");
        messages = page.getElementById("form1:messages").asNormalizedText();
        assertEquals("input1 is filled with 1", "1", input1.getValueAttribute());
        assertEquals("input2 is filled with 1", "1", input2.getValueAttribute());
        assertEquals("input3 is empty", "", input3.getValueAttribute());
        assertEquals("input1 is filled and input2 is filled", "setForm1input1:1\nsetForm1input2:1", messages);

        // fill form1 input3
        input3 = (HtmlTextInput) page.getElementById("form1:inputs:input3");
        input3.setValueAttribute("1");
        input3.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        input1 = (HtmlTextInput) page.getElementById("form1:inputs:input1");
        input2 = (HtmlTextInput) page.getElementById("form1:inputs:input2");
        input3 = (HtmlTextInput) page.getElementById("form1:inputs:input3");
        messages = page.getElementById("form1:messages").asNormalizedText();
        assertEquals("input1 is refreshed to empty string", "", input1.getValueAttribute());
        assertEquals("input2 is refreshed to empty string", "", input2.getValueAttribute());
        assertEquals("input3 is filled and refreshed", "1x", input3.getValueAttribute());
        assertEquals("input3 is filled", "setForm1input3:1", messages);

        // fill form2 input1
        input1 = (HtmlTextInput) page.getElementById("form2:inputs:input1");
        input1.setValueAttribute("1");
        input1.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        input1 = (HtmlTextInput) page.getElementById("form2:inputs:input1");
        input2 = (HtmlTextInput) page.getElementById("form2:inputs:input2");
        input3 = (HtmlTextInput) page.getElementById("form2:inputs:input3");
        messages = page.getElementById("form2:messages").asNormalizedText();
        assertEquals("input1 is filled with 1", "1", input1.getValueAttribute());
        assertEquals("input2 is empty", "", input2.getValueAttribute());
        assertEquals("input3 is empty", "", input3.getValueAttribute());
        assertEquals("input1 is filled and input2 is empty", "setForm2input1:1\nsetForm2input2:", messages);

        // fill form2 input2
        input2 = (HtmlTextInput) page.getElementById("form2:inputs:input2");
        input2.setValueAttribute("1");
        input2.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        input1 = (HtmlTextInput) page.getElementById("form2:inputs:input1");
        input2 = (HtmlTextInput) page.getElementById("form2:inputs:input2");
        input3 = (HtmlTextInput) page.getElementById("form2:inputs:input3");
        messages = page.getElementById("form2:messages").asNormalizedText();
        assertEquals("input1 is filled with 1", "1", input1.getValueAttribute());
        assertEquals("input2 is filled with 1", "1", input2.getValueAttribute());
        assertEquals("input3 is empty", "", input3.getValueAttribute());
        assertEquals("input1 is filled and input2 is filled", "setForm2input1:1\nsetForm2input2:1", messages);

        // fill form2 input3
        input3 = (HtmlTextInput) page.getElementById("form2:inputs:input3");
        input3.setValueAttribute("1");
        input3.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        input1 = (HtmlTextInput) page.getElementById("form2:inputs:input1");
        input2 = (HtmlTextInput) page.getElementById("form2:inputs:input2");
        input3 = (HtmlTextInput) page.getElementById("form2:inputs:input3");
        messages = page.getElementById("form2:messages").asNormalizedText();
        assertEquals("input1 is refreshed to empty string", "", input1.getValueAttribute());
        assertEquals("input2 is refreshed to empty string", "", input2.getValueAttribute());
        assertEquals("input3 is filled and refreshed", "1x", input3.getValueAttribute());
        assertEquals("input3 is filled", "setForm2input3:1", messages);

        // fill form3 input1
        input1 = (HtmlTextInput) page.getElementById("form3:inputs:input1");
        input1.setValueAttribute("1");
        input1.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        input1 = (HtmlTextInput) page.getElementById("form3:inputs:input1");
        input2 = (HtmlTextInput) page.getElementById("form3:inputs:input2");
        input3 = (HtmlTextInput) page.getElementById("form3:inputs:input3");
        messages = page.getElementById("form3:messages").asNormalizedText();
        assertEquals("input1 is refreshed to 1x", "1x", input1.getValueAttribute());
        assertEquals("input2 is refreshed to x", "x", input2.getValueAttribute());
        assertEquals("input3 is empty", "", input3.getValueAttribute());
        assertEquals("input1 is filled and input2 is empty", "setForm3input1:1\nsetForm3input2:", messages);

        // fill form3 input2
        input2 = (HtmlTextInput) page.getElementById("form3:inputs:input2");
        input2.setValueAttribute("1");
        input2.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        input1 = (HtmlTextInput) page.getElementById("form3:inputs:input1");
        input2 = (HtmlTextInput) page.getElementById("form3:inputs:input2");
        input3 = (HtmlTextInput) page.getElementById("form3:inputs:input3");
        messages = page.getElementById("form3:messages").asNormalizedText();
        assertEquals("input1 is refreshed to 1xx", "1xx", input1.getValueAttribute());
        assertEquals("input2 is refreshed to 1x", "1x", input2.getValueAttribute());
        assertEquals("input3 is empty", "", input3.getValueAttribute());
        assertEquals("input1 is filled and input2 is filled", "setForm3input1:1x\nsetForm3input2:1", messages);

        // fill form3 input3
        input3 = (HtmlTextInput) page.getElementById("form3:inputs:input3");
        input3.setValueAttribute("1");
        input3.fireEvent("change");
        webClient.waitForBackgroundJavaScript(3000);
        input1 = (HtmlTextInput) page.getElementById("form3:inputs:input1");
        input2 = (HtmlTextInput) page.getElementById("form3:inputs:input2");
        input3 = (HtmlTextInput) page.getElementById("form3:inputs:input3");
        messages = page.getElementById("form3:messages").asNormalizedText();
        assertEquals("input1 is refreshed to empty string", "", input1.getValueAttribute());
        assertEquals("input2 is refreshed to empty string", "", input2.getValueAttribute());
        assertEquals("input3 is filled and refreshed", "1x", input3.getValueAttribute());
        assertEquals("input3 is filled", "setForm3input3:1", messages);
    }

    private void validateRenderedMarkup(HtmlPage page, String formId, String render) {
        HtmlTextInput input1 = (HtmlTextInput) page.getElementById(formId + ":inputs:input1");
        assertEquals("input1 and input2 are implied as @this", "mojarra.ab(this,event,'change','" + formId + ":inputs:input1 " + formId + ":inputs:input2','" + render + "')", input1.getOnChangeAttribute());

        HtmlTextInput input2 = (HtmlTextInput) page.getElementById(formId + ":inputs:input2");
        assertEquals("input1 and input2 are implied as @this", "mojarra.ab(this,event,'change','" + formId + ":inputs:input1 " + formId + ":inputs:input2','" + render + "')", input2.getOnChangeAttribute());

        HtmlTextInput input3 = (HtmlTextInput) page.getElementById(formId + ":inputs:input3");
        assertEquals("input3 has still its own f:ajax", "mojarra.ab(this,event,'valueChange',0,'@form')", input3.getOnChangeAttribute());
    }

}
