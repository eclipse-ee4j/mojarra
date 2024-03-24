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

import org.junit.jupiter.api.Test;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;

public class HtmlResponseWriterTest {

    /**
     * Test cloneWithWriter method.
     * @throws java.lang.Exception
     */
    @Test
    public void testCloneWithWriter() throws Exception {

        Method method = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        method.setAccessible(true);
        method.invoke(null, new Object[] { null });

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
    }

    /**
     * Test cloneWithWriter method.
     * @throws java.lang.Exception
     */
    @Test
    public void testCloneWithWriter2() throws Exception {

        Method method = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        method.setAccessible(true);
        method.invoke(null, new Object[] { null });

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
    }

    /**
     * Test CDATA.
     * @throws java.lang.Exception
     */
    @Test
    public void testCDATAWithXHTML() throws Exception {

        Method method = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        method.setAccessible(true);
        method.invoke(null, new Object[] { null });

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
