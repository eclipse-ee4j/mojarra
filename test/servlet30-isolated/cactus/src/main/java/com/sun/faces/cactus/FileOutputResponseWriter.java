/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: FileOutputResponseWriter.java,v 1.1 2005/10/18 16:41:33 edburns Exp $
 */



// FileOutputResponseWriter.java

package com.sun.faces.cactus;

import com.sun.faces.renderkit.html_basic.HtmlResponseWriter;
import com.sun.faces.util.Util;

import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * The sole purpose of <B>FileOutputResponseWriter</B> is to wrap an
 * be a ResponseWriter object that writes its
 * output to a file.  <P>
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 * @version $Id: FileOutputResponseWriter.java,v 1.1 2005/10/18 16:41:33 edburns Exp $
 */

public class FileOutputResponseWriter extends ResponseWriter {

//
// Protected Constants
//

//
// Class Variables
//

//
// Instance Variables
//
    protected PrintWriter out = null;
    public static String FACES_RESPONSE_ROOT = null;
    public static String RESPONSE_WRITER_FILENAME = "ResponseWriter.txt";
    protected HtmlResponseWriter writer = null;
// Attribute Instance Variables


// Relationship Instance Variables

//
// Constructors and Initializers    
//
  
    public FileOutputResponseWriter(String rootDir) {
        try {
            initializeFacesResponseRoot(rootDir);
            File file = new File(RESPONSE_WRITER_FILENAME);
            FileOutputStream fs = new FileOutputStream(file);
            out = new PrintWriter(fs);
            writer = new HtmlResponseWriter(out, "text/html", "ISO-8859-1");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert (false);
        }
    }

//
// Class methods
//

    public static void initializeFacesResponseRoot(String testRootDir) {
        if (null == FACES_RESPONSE_ROOT) {
            assert (null != testRootDir);
            FACES_RESPONSE_ROOT = testRootDir + "/";
            RESPONSE_WRITER_FILENAME =
                FACES_RESPONSE_ROOT + RESPONSE_WRITER_FILENAME;
            
            FileOutputResponseWrapper.FACES_RESPONSE_FILENAME =
                FACES_RESPONSE_ROOT +
                FileOutputResponseWrapper.FACES_RESPONSE_FILENAME;
        }
    }

//
// Methods from Writer
//

    public void write(int c) throws IOException {
        writer.write(c);
    }


    public void write(char[] cbuf) throws IOException {
        writer.write(cbuf);
    }


    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }


    public void write(String str) throws IOException {
        writer.write(str);
    }


    public void write(String str, int off, int len) throws IOException {
        writer.write(str, off, len);
    }


    public void flush() throws IOException {
        writer.flush();
        out.flush();
    }


    public void close() throws IOException {
        writer.close();
        out.close();
    }


    public void writeText(char[] text, int off, int len) throws IOException {
        writer.writeText(text, off, len);
    }


    public void writeText(Object text, String componentPropertyName)
        throws IOException {
        writer.writeText(text, componentPropertyName);
    }


    public void writeComment(Object text) throws IOException {
        writer.writeComment(text);
    }


    public void writeAttribute(String name, Object value, String componentPropertyName)
        throws IOException {
        writer.writeAttribute(name, value, componentPropertyName);
    }


    public void writeURIAttribute(String name, Object value, String componentPropertyName)
        throws IOException {
        writer.writeURIAttribute(name, value, componentPropertyName);
    }


    public void startElement(String name, UIComponent componentForElement)
        throws IOException {
        writer.startElement(name, componentForElement);
    }


    public void endElement(String name) throws IOException {
        writer.endElement(name);
    }


    public void startDocument() throws IOException {
        writer.startDocument();
    }


    public void endDocument() throws IOException {
        writer.endDocument();
    }


    public ResponseWriter cloneWithWriter(Writer writer) {
        return this.writer.cloneWithWriter(writer);
    }


    public String getCharacterEncoding() {
        return writer.getCharacterEncoding();
    }


    public String getContentType() {
        return writer.getContentType();
    }

} // end of class FileOutputResponseWriter


