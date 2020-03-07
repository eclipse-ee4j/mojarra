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

package jakarta.faces.context;

import java.io.IOException;
import java.io.Writer;

import jakarta.faces.FacesWrapper;
import jakarta.faces.component.UIComponent;

/**
 * <p>
 * <span class="changed_modified_2_0 changed_modified_2_3">Provides</span> a simple implementation of
 * {@link ResponseWriter} that can be subclassed by developers wishing to provide specialized behavior to an existing
 * {@link ResponseWriter} instance. The default implementation of all methods is to call through to the wrapped
 * {@link ResponseWriter}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 1.2
 */
public abstract class ResponseWriterWrapper extends ResponseWriter implements FacesWrapper<ResponseWriter> {

    private ResponseWriter wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ResponseWriterWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this response writer has been decorated, the implementation doing the decorating should push the implementation
     * being wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public ResponseWriterWrapper(ResponseWriter wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ResponseWriter getWrapped() {
        return wrapped;
    }

    // -------------------------- Methods from jakarta.faces.context.ResponseWriter

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#getContentType()} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#getContentType()
     * @since 1.2
     */
    @Override
    public String getContentType() {

        return getWrapped().getContentType();

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#getCharacterEncoding()} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#getCharacterEncoding()
     * @since 1.2
     */
    @Override
    public String getCharacterEncoding() {

        return getWrapped().getCharacterEncoding();

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#flush()} on the wrapped {@link ResponseWriter}
     * object.
     * </p>
     *
     * @see ResponseWriter#flush()
     * @since 1.2
     */
    @Override
    public void flush() throws IOException {

        getWrapped().flush();

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#startDocument()} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#startDocument()
     * @since 1.2
     */
    @Override
    public void startDocument() throws IOException {

        getWrapped().startDocument();

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#endDocument()} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#endDocument()
     * @since 1.2
     */
    @Override
    public void endDocument() throws IOException {

        getWrapped().endDocument();

    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ResponseWriter#startElement(String, jakarta.faces.component.UIComponent)} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#startElement(String, jakarta.faces.component.UIComponent)
     * @since 1.2
     */
    @Override
    public void startElement(String name, UIComponent component) throws IOException {

        getWrapped().startElement(name, component);

    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link ResponseWriter#startCDATA} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @since 2.0
     * @throws IOException on any read/write error
     */
    @Override
    public void startCDATA() throws IOException {
        getWrapped().startCDATA();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link ResponseWriter#endCDATA} on the wrapped {@link ResponseWriter}
     * object.
     * </p>
     *
     * @since 2.0
     * @throws IOException on any read/write error
     */
    @Override
    public void endCDATA() throws IOException {
        getWrapped().endCDATA();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#endElement(String)} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#endElement(String)
     * @since 1.2
     * @throws IOException on any read/write error
     */
    @Override
    public void endElement(String name) throws IOException {

        getWrapped().endElement(name);

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#writeAttribute(String, Object, String)} on the
     * wrapped {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#writeAttribute(String, Object, String)
     * @since 1.2
     */
    @Override
    public void writeAttribute(String name, Object value, String property) throws IOException {

        getWrapped().writeAttribute(name, value, property);

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#writeURIAttribute(String, Object, String)} on
     * the wrapped {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#writeURIAttribute(String, Object, String)
     * @since 1.2
     */
    @Override
    public void writeURIAttribute(String name, Object value, String property) throws IOException {

        getWrapped().writeURIAttribute(name, value, property);

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#writeComment(Object)} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#writeComment(Object)
     * @since 1.2
     */
    @Override
    public void writeComment(Object comment) throws IOException {

        getWrapped().writeComment(comment);

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#writeDoctype} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#writeDoctype
     * @since 2.2
     */
    @Override
    public void writeDoctype(String doctype) throws IOException {
        getWrapped().writeDoctype(doctype);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#writePreamble} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#writePreamble
     * @since 2.2
     */
    @Override
    public void writePreamble(String preamble) throws IOException {
        getWrapped().writePreamble(preamble);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#writeText(Object, String)} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#writeText(Object, String)
     * @since 1.2
     */
    @Override
    public void writeText(Object text, String property) throws IOException {

        getWrapped().writeText(text, property);

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#writeText(Object, UIComponent, String)} on the
     * wrapped {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#writeText(Object, String)
     * @since 1.2
     */

    @Override
    public void writeText(Object text, UIComponent component, String property) throws IOException {
        getWrapped().writeText(text, component, property);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#writeText(char[], int, int)} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#writeText(char[], int, int)
     * @since 1.2
     */
    @Override
    public void writeText(char[] text, int off, int len) throws IOException {

        getWrapped().writeText(text, off, len);

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#cloneWithWriter(java.io.Writer)} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#cloneWithWriter(java.io.Writer)
     * @since 1.2
     */
    @Override
    public ResponseWriter cloneWithWriter(Writer writer) {

        return getWrapped().cloneWithWriter(writer);

    }

    // --------------------------------------------- Methods from java.io.Writer

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#close()} on the wrapped {@link ResponseWriter}
     * object.
     * </p>
     *
     * @see ResponseWriter#close()
     * @since 1.2
     */
    @Override
    public void close() throws IOException {

        getWrapped().close();

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ResponseWriter#write(char[], int, int)} on the wrapped
     * {@link ResponseWriter} object.
     * </p>
     *
     * @see ResponseWriter#write(char[], int, int)
     * @since 1.2
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {

        getWrapped().write(cbuf, off, len);

    }

}
