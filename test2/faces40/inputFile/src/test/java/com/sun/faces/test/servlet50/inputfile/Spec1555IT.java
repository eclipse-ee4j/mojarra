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

package com.sun.faces.test.servlet50.inputfile;

import static java.lang.System.getProperty;
import static java.nio.file.StandardOpenOption.APPEND;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.helpers.AttributesImpl;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.parser.neko.HtmlUnitNekoHtmlParser;

@Ignore // Failing because request.getParameter() returns null for all params since jakarta.servlet-api:6.0.0 (worked fine in 5.0.0!) -- TODO remove once Servlet API or GlassFish is fixed 
@RunWith(Arquillian.class)
public class Spec1555IT {

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
    public void testSingleSelectionNonAjax() throws Exception {
        testSingleSelection("singleSelectionFormNonAjax");
    }

    @Test
    public void testSingleSelectionAjax() throws Exception {
        testSingleSelection("singleSelectionFormAjax");
    }

    private void testSingleSelection(String form) throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1555IT.xhtml");
        HtmlFileInput input = page.getHtmlElementById(form + ":input");

        assertEquals("Multiple attribute is NOT set", "", input.getAttribute("multiple"));

        File file = generateTempFile("file", "bin", 123);
        input.setValueAttribute(file.getAbsolutePath());

        page = page.getHtmlElementById(form + ":submit").click();
        
        assertEquals("Value attribute is NOT set", "", page.getHtmlElementById(form + ":input").getAttribute("value"));

        HtmlElement messages = page.getHtmlElementById("messages");

        assertEquals("There is 1 message", 1, messages.getChildElementCount());

        DomElement message = messages.getChildElements().iterator().next();

        assertEquals("Uploaded file has been received", "field: singleSelection, name: " + file.getName() + ", size: " + file.length(), message.asNormalizedText());
    }

    @Test
    @Ignore
    public void testMultipleSelectionNonAjax() throws Exception {
        testMultipleSelection("multipleSelectionFormNonAjax");
    }

    @Test
    public void testMultipleSelectionAjax() throws Exception {
        testMultipleSelection("multipleSelectionFormAjax");
    }

    private void testMultipleSelection(String form) throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1555IT.xhtml");
        HtmlFileInput input = page.getHtmlElementById(form + ":input");

        assertEquals("Multiple attribute is set", "multiple", input.getAttribute("multiple"));

        File file1 = generateTempFile("file1", "bin", 123);
        File file2 = generateTempFile("file2", "bin", 234);
        File file3 = generateTempFile("file3", "bin", 345);
        input.setValueAttribute(file1.getAbsolutePath());
        addValueAttribute(input, file2.getAbsolutePath());
        addValueAttribute(input, file3.getAbsolutePath());
        page = page.getHtmlElementById(form + ":submit").click();

        assertEquals("Value attribute is NOT set", "", page.getHtmlElementById(form + ":input").getAttribute("value"));

        HtmlElement messages = page.getHtmlElementById("messages");

        assertEquals("There are 3 messages", 3, messages.getChildElementCount());

        Iterator<DomElement> iterator = messages.getChildElements().iterator();
        DomElement message1 = iterator.next();
        DomElement message2 = iterator.next();
        DomElement message3 = iterator.next();

        assertEquals("First uploaded file has been received", "field: multipleSelection, name: " + file1.getName() + ", size: " + file1.length(), message1.asNormalizedText());
        assertEquals("Second uploaded file has been received", "field: multipleSelection, name: " + file2.getName() + ", size: " + file2.length(), message2.asNormalizedText());
        assertEquals("Third uploaded file has been received", "field: multipleSelection, name: " + file3.getName() + ", size: " + file3.length(), message3.asNormalizedText());
    }

    private static File generateTempFile(String name, String ext, int size) throws IOException {
        Path path = Files.createTempFile(name, "." + ext);
        byte[] content = new byte[size];
        Files.write(path, content, APPEND);
        return path.toFile();
    }

    /**
     * HtmlUnit's HtmlFileInput doesn't support submitting multiple values.
     * The below is a work-around, found on https://stackoverflow.com/a/19654060
     */
    private static void addValueAttribute(HtmlFileInput input, String valueAttribute) {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(null, null, "type", null, "file");
        attributes.addAttribute(null, null, "name", null, input.getNameAttribute());
        HtmlFileInput cloned = (HtmlFileInput) new HtmlUnitNekoHtmlParser().getFactory("input").createElementNS(input.getPage(), null, "input", attributes);
        input.getParentNode().appendChild(cloned);
        cloned.setValueAttribute(valueAttribute);
    }

}
