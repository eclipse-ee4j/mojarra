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

package com.sun.faces.test.servlet30.facesContext;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Locale;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import static org.junit.Assert.*;

/**
 * The managed bean for the accessor tests.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@ManagedBean(name = "accessorBean")
@RequestScoped
public class AccessorBean implements Serializable {

    public String getAccessorResult1() {
        UIViewRoot oldRoot = FacesContext.getCurrentInstance().getViewRoot();
        UIViewRoot page = new UIViewRoot();
        page.setViewId("viewId");
        page.setLocale(Locale.US);
        FacesContext.getCurrentInstance().setViewRoot(page);
        UIViewRoot root = FacesContext.getCurrentInstance().getViewRoot();
        assertNotNull(root);
        assertEquals(root, FacesContext.getCurrentInstance().getViewRoot());
        FacesContext.getCurrentInstance().setViewRoot(oldRoot);
        return "PASSED";
    }

    public String getAccessorResult2() {
        ResponseStream oldStream = FacesContext.getCurrentInstance().getResponseStream();
        ResponseStream responseStream = new ResponseStream() {

            public void write(int b) {
            }
        };
        FacesContext.getCurrentInstance().setResponseStream(responseStream);
        assertNotNull(FacesContext.getCurrentInstance().getResponseStream());
        assertEquals(responseStream, FacesContext.getCurrentInstance().getResponseStream());
        if (oldStream != null) {
            FacesContext.getCurrentInstance().setResponseStream(oldStream);
        }
        return "PASSED";
    }

    public String getAccessorResult3() {
        ResponseWriter oldWriter = FacesContext.getCurrentInstance().getResponseWriter();
        ResponseWriter responseWriter = new ResponseWriter() {

            @Override
            public Writer append(CharSequence csq) throws IOException {
                return super.append(csq);
            }

            ;
            @Override
            public String getContentType() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getCharacterEncoding() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void flush() throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void startDocument() throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void endDocument() throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void startElement(String name, UIComponent component) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void endElement(String name) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void writeAttribute(String name, Object value, String property) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void writeURIAttribute(String name, Object value, String property) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void writeComment(Object comment) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void writeText(Object text, String property) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void writeText(char[] text, int off, int len) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ResponseWriter cloneWithWriter(Writer writer) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void close() throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        FacesContext.getCurrentInstance().setResponseWriter(responseWriter);
        assertNotNull(FacesContext.getCurrentInstance().getResponseWriter());
        assertEquals(responseWriter, FacesContext.getCurrentInstance().getResponseWriter());

        try {
            FacesContext.getCurrentInstance().setResponseWriter(null);
            fail();
        } catch (Exception exception) {
        }
        
        FacesContext.getCurrentInstance().setResponseWriter(oldWriter);
        return "PASSED";
    }
}
