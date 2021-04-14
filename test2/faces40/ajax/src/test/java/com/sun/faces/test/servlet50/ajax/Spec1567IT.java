/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
    public void testAjaxExecute() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1567IT.xhtml");

        validateRenderedMarkupOfExecuteAttribute(page, "form1", "0");
        validateRenderedMarkupOfExecuteAttribute(page, "form2", "0");
        validateRenderedMarkupOfExecuteAttribute(page, "form3", "'@this'");
    }

    private void validateRenderedMarkupOfExecuteAttribute(HtmlPage page, String formId, String render) {
        HtmlTextInput input1 = (HtmlTextInput) page.getElementById(formId + ":inputs:input1");
        assertEquals("input1 and input2 are implied as @this", "mojarra.ab(this,event,'change','" + formId + ":inputs:input1 " + formId + ":inputs:input2'," + render + ")", input1.getOnChangeAttribute());

        HtmlTextInput input2 = (HtmlTextInput) page.getElementById(formId + ":inputs:input2");
        assertEquals("input1 and input2 are implied as @this", "mojarra.ab(this,event,'change','" + formId + ":inputs:input1 " + formId + ":inputs:input2'," + render + ")", input2.getOnChangeAttribute());

        HtmlTextInput input3 = (HtmlTextInput) page.getElementById(formId + ":inputs:input3");
        assertEquals("input3 has still its own f:ajax", "mojarra.ab(this,event,'valueChange',0,0)", input3.getOnChangeAttribute());
    }

}
