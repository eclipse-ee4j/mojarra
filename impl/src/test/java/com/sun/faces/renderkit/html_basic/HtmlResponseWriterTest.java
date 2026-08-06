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

package com.sun.faces.renderkit.html_basic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

public class HtmlResponseWriterTest {

    @BeforeEach
    public void clearFacesContext() throws Exception {
        Method setCurrent = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrent.setAccessible(true);
        setCurrent.invoke(null, new Object[] { null });
    }

    /**
     * Test cloneWithWriter method.
     * @throws java.lang.Exception
     */
    @Test
    public void testCloneWithWriter() throws Exception {
        Writer writer = new StringWriter();
        HtmlResponseWriter responseWriter = new HtmlResponseWriter(writer, "text/html", "UTF-8");
        Field field = responseWriter.getClass().getDeclaredField("dontEscape");
        field.setAccessible(true);
        field.set(responseWriter, Boolean.TRUE);

        HtmlResponseWriter clonedWriter = (HtmlResponseWriter) responseWriter.cloneWithWriter(writer);
        assertTrue((Boolean) field.get(clonedWriter));

        responseWriter = new HtmlResponseWriter(writer, "text/html", "UTF-8");
        field.set(responseWriter, Boolean.FALSE);

        clonedWriter = (HtmlResponseWriter) responseWriter.cloneWithWriter(writer);
        assertFalse((Boolean) field.get(clonedWriter));
        responseWriter.close();
        clonedWriter.close();
    }

    /**
     * Test cloneWithWriter method.
     * @throws java.lang.Exception
     */
    @Test
    public void testCloneWithWriter2() throws Exception {
        Writer writer = new StringWriter();
        HtmlResponseWriter responseWriter = new HtmlResponseWriter(writer, "text/html", "UTF-8");
        Field field = responseWriter.getClass().getDeclaredField("writingCdata");
        field.setAccessible(true);
        field.set(responseWriter, Boolean.TRUE);

        HtmlResponseWriter clonedWriter = (HtmlResponseWriter) responseWriter.cloneWithWriter(writer);
        assertTrue((Boolean) field.get(clonedWriter));

        responseWriter = new HtmlResponseWriter(writer, "text/html", "UTF-8");
        field.set(responseWriter, Boolean.FALSE);

        clonedWriter = (HtmlResponseWriter) responseWriter.cloneWithWriter(writer);
        assertFalse((Boolean) field.get(clonedWriter));
        responseWriter.close();
        clonedWriter.close();
    }

    /**
     * Test CDATA.
     * @throws java.lang.Exception
     */
    @Test
    public void testCDATAWithXHTML() throws Exception {
        UIComponent componentForElement = new UIOutput();
        String expected = "<script>\n//<![CDATA[\n\n function queueEvent() {\n  return false;\n}\n\n\n//]]>\n</script>";

        // Case 1 start is // end is //
        StringWriter stringWriter = new StringWriter();
        HtmlResponseWriter responseWriter = new HtmlResponseWriter(stringWriter, "application/xhtml+xml", "UTF-8");
        responseWriter.startElement("script", componentForElement);
        responseWriter.write("    // <![CDATA[\n function queueEvent() {\n  return false;\n}\n\n//   ]]>  \n");
        responseWriter.endElement("script");
        responseWriter.flush();
        assertEquals(expected, stringWriter.toString());
        responseWriter.close();

        // Case 2 start is // end is /* */
        stringWriter = new StringWriter();
        responseWriter = new HtmlResponseWriter(stringWriter, "application/xhtml+xml", "UTF-8");
        responseWriter.startElement("script", componentForElement);
        responseWriter.write("    // <![CDATA[\n function queueEvent() {\n  return false;\n}\n\n/*\n  ]]> \n*/ \n");
        responseWriter.endElement("script");
        responseWriter.flush();
        assertEquals(expected, stringWriter.toString());
        responseWriter.close();

        // Case 3 start is /* */  end is /* */
        stringWriter = new StringWriter();
        responseWriter = new HtmlResponseWriter(stringWriter, "application/xhtml+xml", "UTF-8");
        responseWriter.startElement("script", componentForElement);
        responseWriter.write("    /* \n <![CDATA[ \n*/\n function queueEvent() {\n  return false;\n}\n\n/*\n  ]]> \n*/ \n");
        responseWriter.endElement("script");
        responseWriter.flush();
        assertEquals(expected, stringWriter.toString());
        responseWriter.close();

        // Case 4 start is /* */  end is //
        stringWriter = new StringWriter();
        responseWriter = new HtmlResponseWriter(stringWriter, "application/xhtml+xml", "UTF-8");
        responseWriter.startElement("script", componentForElement);
        responseWriter.write("    /* \n <![CDATA[ \n*/\n function queueEvent() {\n  return false;\n}\n\n//\n  ]]>\n");
        responseWriter.endElement("script");
        responseWriter.flush();
        assertEquals(expected, stringWriter.toString());
        responseWriter.close();

        // Case 5 start is /* */  end is //
        stringWriter = new StringWriter();
        responseWriter = new HtmlResponseWriter(stringWriter, "application/xhtml+xml", "UTF-8");
        responseWriter.startElement("script", componentForElement);
        responseWriter.write("    /* \n <![CDATA[ \n*/\n function queueEvent() {\n  return false;\n}\n\n//\n  ]]>\n");
        responseWriter.endElement("script");
        responseWriter.flush();
        assertEquals(expected, stringWriter.toString());
        responseWriter.close();
    }

    /**
     * Test CDATA escaping.
     */
    @Test
    public void testUnescapedCDATA() throws Exception {
        String expectedStart = "<style>\n<![CDATA[\n";
        String expectedEnd = "\n]]>\n</style>";

        // single char
        StringWriter stringWriter = new StringWriter();
        HtmlResponseWriter responseWriter = new HtmlResponseWriter(stringWriter, "application/xhtml+xml", "UTF-8");
        responseWriter.startElement("style", null);
        responseWriter.startCDATA();
        responseWriter.writeText(']', null);
        responseWriter.endCDATA();
        responseWriter.endElement("style");
        responseWriter.flush();
        assertEquals(expectedStart + "]]]><![CDATA[" + expectedEnd, stringWriter.toString());
        responseWriter.close();

        // two chars
        stringWriter = new StringWriter();
        responseWriter = new HtmlResponseWriter(stringWriter, "application/xhtml+xml", "UTF-8");
        responseWriter.startElement("style", null);
        responseWriter.startCDATA();
        responseWriter.writeText("]]".toCharArray());
        responseWriter.endCDATA();
        responseWriter.endElement("style");
        responseWriter.flush();
        assertEquals(expectedStart + "]]]><![CDATA[]]]><![CDATA[" + expectedEnd, stringWriter.toString());
        responseWriter.close();

        // long string
        stringWriter = new StringWriter();
        responseWriter = new HtmlResponseWriter(stringWriter, "application/xhtml+xml", "UTF-8");
        responseWriter.startElement("style", null);
        responseWriter.startCDATA();
        responseWriter.writeText("abc]]>abc", null);
        responseWriter.endCDATA();
        responseWriter.endElement("style");
        responseWriter.flush();
        assertEquals(expectedStart + "abc]]]><![CDATA[]>abc" + expectedEnd, stringWriter.toString());
        responseWriter.close();
    }

    /**
     * Wire ordering invariant: regular attributes appear FIRST, pass-through attributes appear
     * LAST within a single start tag. The Jakarta Faces spec does not mandate this -- HTML
     * treats attribute order within a tag as semantically irrelevant -- but consistent ordering
     * matters for byte-equivalent caching, snapshot tests, and downstream consumers that pattern-
     * match the rendered output.
     */
    @Test
    public void testRegularAttributesPrecedePassThroughAttributes() throws Exception {
        UIComponent component = new UIOutput();
        component.getPassThroughAttributes(true).put("data-x", "1");
        component.getPassThroughAttributes(true).put("data-y", "2");

        StringWriter sw = new StringWriter();
        HtmlResponseWriter writer = new HtmlResponseWriter(sw, "text/html", "UTF-8");
        writer.startElement("div", component);
        writer.writeAttribute("id", "regular", null);
        writer.endElement("div");
        writer.flush();

        String output = sw.toString();
        // both passthrough indices are GREATER than the regular 'id' index
        assertTrue(output.indexOf(" id=") < output.indexOf("data-x"), output);
        assertTrue(output.indexOf(" id=") < output.indexOf("data-y"), output);
        writer.close();
    }

    /**
     * A regular writeAttribute whose name collides with a pass-through attribute must be
     * suppressed (the pass-through value wins). Locks in the containsPassThroughAttribute check.
     */
    @Test
    public void testRegularAttributeWithPassThroughNameIsSuppressed() throws Exception {
        UIComponent component = new UIOutput();
        component.getPassThroughAttributes(true).put("title", "passthroughWins");

        StringWriter sw = new StringWriter();
        HtmlResponseWriter writer = new HtmlResponseWriter(sw, "text/html", "UTF-8");
        writer.startElement("div", component);
        writer.writeAttribute("title", "regularLoses", null);
        writer.endElement("div");
        writer.flush();

        String output = sw.toString();
        assertTrue(output.contains("title=\"passthroughWins\""), output);
        assertFalse(output.contains("regularLoses"), output);
        writer.close();
    }

    /**
     * Empty elements (e.g. {@code <input/>}) still emit pass-through attributes followed by the
     * self-closing {@code " />"} sequence.
     */
    @Test
    public void testEmptyElementEmitsPassThroughThenSelfClose() throws Exception {
        UIComponent component = new UIOutput();
        component.getPassThroughAttributes(true).put("data-flag", "yes");

        StringWriter sw = new StringWriter();
        HtmlResponseWriter writer = new HtmlResponseWriter(sw, "application/xhtml+xml", "UTF-8");
        writer.startElement("input", component);
        writer.endElement("input");
        writer.flush();

        assertEquals("<input data-flag=\"yes\" />", sw.toString());
        writer.close();
    }

    /**
     * Empty elements with both regular and pass-through attributes follow the same ordering as
     * non-empty elements: regular first, pass-through last, then {@code " />"}.
     */
    @Test
    public void testEmptyElementWithMixedAttributesPreservesOrder() throws Exception {
        UIComponent component = new UIOutput();
        component.getPassThroughAttributes(true).put("data-flag", "yes");

        StringWriter sw = new StringWriter();
        HtmlResponseWriter writer = new HtmlResponseWriter(sw, "application/xhtml+xml", "UTF-8");
        writer.startElement("input", component);
        writer.writeAttribute("type", "text", null);
        writer.endElement("input");
        writer.flush();

        assertEquals("<input type=\"text\" data-flag=\"yes\" />", sw.toString());
        writer.close();
    }

    /**
     * Pass-through attributes are emitted in the order they were set on the component, so a view renders
     * byte-identically across requests and JVMs rather than in a hash order.
     */
    @Test
    public void testPassThroughAttributesAreEmittedInTheOrderTheyWereSet() throws Exception {
        UIComponent component = new UIOutput();
        Map<String, Object> passThroughAttributes = component.getPassThroughAttributes(true);
        passThroughAttributes.put("data-zulu", "1");
        passThroughAttributes.put("data-alpha", "2");
        passThroughAttributes.put("data-mike", "3");

        StringWriter sw = new StringWriter();
        HtmlResponseWriter writer = new HtmlResponseWriter(sw, "text/html", "UTF-8");
        writer.startElement("div", component);
        writer.endElement("div");
        writer.flush();

        assertEquals("<div data-zulu=\"1\" data-alpha=\"2\" data-mike=\"3\"></div>", sw.toString());
        writer.close();
    }

    /**
     * Rendering must not mutate the component's own pass-through attribute map: the writer reads it in place, and a
     * component renders more than once (ajax re-render, a second view of the same tree).
     */
    @Test
    public void testRenderingLeavesTheComponentPassThroughAttributesIntact() throws Exception {
        UIComponent component = new UIOutput();
        component.getPassThroughAttributes(true).put("data-x", "1");
        component.getPassThroughAttributes(true).put(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY, "section");

        StringWriter sw = new StringWriter();
        HtmlResponseWriter writer = new HtmlResponseWriter(sw, "text/html", "UTF-8");
        writer.startElement("div", component);
        writer.endElement("div");
        writer.flush();
        writer.close();

        assertEquals(2, component.getPassThroughAttributes(false).size());
        assertEquals("section", component.getPassThroughAttributes(false).get(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY));
    }

    /**
     * The {@code elementName} pass-through attribute renames the element and is itself never emitted as an attribute,
     * whichever other pass-through attributes accompany it.
     */
    @Test
    public void testElementNameRenamesTheElementAndIsNotEmitted() throws Exception {
        UIComponent component = new UIOutput();
        component.getPassThroughAttributes(true).put(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY, "section");
        component.getPassThroughAttributes(true).put("data-x", "1");

        StringWriter sw = new StringWriter();
        HtmlResponseWriter writer = new HtmlResponseWriter(sw, "text/html", "UTF-8");
        writer.startElement("div", component);
        writer.endElement("div");
        writer.flush();

        assertEquals("<section data-x=\"1\"></section>", sw.toString());
        writer.close();
    }

    /**
     * An {@code elementName} that is the only pass-through attribute renames the element and leaves nothing to emit.
     */
    @Test
    public void testLoneElementNameRenamesTheElement() throws Exception {
        UIComponent component = new UIOutput();
        component.getPassThroughAttributes(true).put(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY, "section");

        StringWriter sw = new StringWriter();
        HtmlResponseWriter writer = new HtmlResponseWriter(sw, "text/html", "UTF-8");
        writer.startElement("div", component);
        writer.writeAttribute("id", "regular", null);
        writer.endElement("div");
        writer.flush();

        assertEquals("<section id=\"regular\"></section>", sw.toString());
        writer.close();
    }

    /**
     * Test CDATA escaping with zero length content.
     */
    @Test
    public void testUnescapedCDataWithZeroLengthContent() throws Exception {
        String expectedStart = "<style>\n<![CDATA[\n";
        String expectedEnd = "\n]]>\n</style>";

        // make sure empty string is handle correct
        StringWriter stringWriter = new StringWriter();
        HtmlResponseWriter responseWriter = new HtmlResponseWriter(stringWriter, "application/xhtml+xml", "UTF-8");
        responseWriter.startElement("style", null);
        responseWriter.startCDATA();
        responseWriter.writeText("".toCharArray());
        responseWriter.writeText("", null);
        responseWriter.endCDATA();
        responseWriter.endElement("style");
        responseWriter.flush();
        assertEquals(expectedStart + expectedEnd, stringWriter.toString());
        responseWriter.close();
    }
}
