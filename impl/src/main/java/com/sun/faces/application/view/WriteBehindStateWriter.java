/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.application.view;

import static jakarta.faces.application.StateManager.IS_SAVING_STATE;
import static java.lang.Boolean.TRUE;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.sun.faces.RIConstants;
import com.sun.faces.io.FastStringWriter;
import com.sun.faces.util.Util;

import jakarta.faces.application.StateManager;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * Custom {@link Writer} to efficiently handle the state manager replacement marker written out by
 * {@link MultiViewHandler#writeState(jakarta.faces.context.FacesContext)}.
 */
final class WriteBehindStateWriter extends Writer {

    // length of the state marker
    private static final int STATE_MARKER_LEN = RIConstants.SAVESTATE_FIELD_MARKER.length();

    private static final ThreadLocal<WriteBehindStateWriter> CUR_WRITER = new ThreadLocal<>();
    private Writer out;
    private final Writer orig;
    private ChunkedWriter fWriter;
    private boolean stateWritten;
    private int markersRequested;
    private final int bufSize;
    private final char[] buf;
    private final FacesContext context;
    private Object state;

    // -------------------------------------------------------- Constructors

    /**
     * Constructs a new <code>WriteBehindStateWriter</code> instance.
     *
     * @param out the writer we write non-buffered content to
     * @param context the {@link FacesContext} for the current request
     * @param bufSize the buffer size for post-processing buffered content
     */
    public WriteBehindStateWriter(Writer out, FacesContext context, int bufSize) {
        this.out = out;
        orig = out;
        this.context = context;
        this.bufSize = bufSize;
        buf = new char[bufSize];
        CUR_WRITER.set(this);
    }

    // ------------------------------------------------- Methods from Writer

    /**
     * Writes directly to the current <code>out</code>.
     *
     * @see Writer#write(int)
     */
    @Override
    public void write(int c) throws IOException {
        out.write(c);
    }

    /**
     * Writes directly to the current <code>out</code>.
     *
     * @see Writer#write(char[])
     */
    @Override
    public void write(char[] cbuf) throws IOException {
        out.write(cbuf);
    }

    /**
     * Writes directly to the current <code>out</code>.
     *
     * @see Writer#write(String)
     */
    @Override
    public void write(String str) throws IOException {
        out.write(str);
    }

    /**
     * Writes directly to the current <code>out</code>.
     *
     * @see Writer#write(String, int, int)
     */
    @Override
    public void write(String str, int off, int len) throws IOException {
        out.write(str, off, len);
    }

    /**
     * Writes directly to the current <code>out</code>.
     *
     * @see Writer#write(char[], int, int)
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }

    /**
     * This is a no-op.
     */
    @Override
    public void flush() throws IOException {
        // no-op
    }

    /**
     * This is a no-op.
     */
    @Override
    public void close() throws IOException {
        // no-op
    }

    // ------------------------------------------------------ Public Methods

    /**
     * @return the <code>WriteBehindStateWriter</code> being used for processing this request
     */
    public static WriteBehindStateWriter getCurrentInstance() {
        return CUR_WRITER.get();
    }

    /**
     * Clear the ThreadLocal state.
     */
    public void release() {
        CUR_WRITER.remove();
    }

    /**
     * When called, the original writer is backed up and replaced with a new chunked buffer. All content written after
     * this method is called will then be buffered and written out later after the entire view has been rendered.
     */
    public void writingState() {
        markersRequested++;
        if (!stateWritten) {
            stateWritten = true;
            out = fWriter = new ChunkedWriter();
        }
    }

    /**
     * @return <code>true</code> if {@link #writingState()} has been called, otherwise returns <code>false</code>
     */
    public boolean stateWritten() {
        return stateWritten;
    }

    /**
     * <p>
     * Write directly from our buffer to the provided writer.
     * </p>
     *
     * @throws IOException if an error occurs
     */
    public void flushToWriter() throws IOException {

        // Save the state to a new instance of StringWriter to
        // avoid multiple serialization steps if the view contains
        // multiple forms.
        StateManager stateManager = Util.getStateManager(context);
        ResponseWriter origWriter = context.getResponseWriter();
        StringBuilder stateBuilder = getState(stateManager, origWriter);

        if (fWriter.markerCount == markersRequested) {
            // Every marker arrived whole, so its position is known: write the buffered chunks straight to the
            // response around them, without searching the content or copying it again.
            int pos = 0;
            for (int i = 0; i < fWriter.markerCount; i++) {
                int markerPos = fWriter.markers[i];
                fWriter.writeTo(orig, pos, markerPos);
                writeStateContent(stateBuilder);
                pos = markerPos + STATE_MARKER_LEN;
                stateBuilder = getState(stateManager, origWriter);
            }
            fWriter.writeTo(orig, pos, fWriter.length());
            out = orig;
            return;
        }

        // A marker was split across writes (e.g. by a buffering ResponseWriter wrapper): search the content for it.
        StringBuilder builder = fWriter.toStringBuilder();

        // Begin writing...
        int totalLen = builder.length();
        int stateLen = stateBuilder.length();
        int pos = 0;
        int tildeIdx = getNextDelimiterIndex(builder, pos);
        while (pos < totalLen) {
            if (tildeIdx != -1) {
                if (tildeIdx > pos && tildeIdx - pos > bufSize) {
                    // There's enough content before the first ~
                    // to fill the entire buffer
                    builder.getChars(pos, pos + bufSize, buf, 0);
                    orig.write(buf);
                    pos += bufSize;
                } else {
                    // Write all content up to the first '~'
                    builder.getChars(pos, tildeIdx, buf, 0);
                    int len = tildeIdx - pos;
                    orig.write(buf, 0, len);
                    // Now check to see if the state saving string is
                    // at the beginning of pos, if so, write our
                    // state out.
                    if (builder.indexOf(RIConstants.SAVESTATE_FIELD_MARKER, pos) == tildeIdx) {
                        // buf is effectively zero'd out at this point
                        int statePos = 0;
                        while (statePos < stateLen) {
                            if (stateLen - statePos > bufSize) {
                                // enough state to fill the buffer
                                stateBuilder.getChars(statePos, statePos + bufSize, buf, 0);
                                orig.write(buf);
                                statePos += bufSize;
                            } else {
                                int slen = stateLen - statePos;
                                stateBuilder.getChars(statePos, stateLen, buf, 0);
                                orig.write(buf, 0, slen);
                                statePos += slen;
                            }

                        }
                        // Push us past the last '~' at the end of the marker
                        pos += len + STATE_MARKER_LEN;
                        tildeIdx = getNextDelimiterIndex(builder, pos);

                        stateBuilder = getState(stateManager, origWriter);
                        stateLen = stateBuilder.length();
                    } else {
                        pos = tildeIdx;
                        tildeIdx = getNextDelimiterIndex(builder, tildeIdx + 1);
                    }
                }
            } else {
                // We've written all of the state field markers.
                // finish writing content
                if (totalLen - pos > bufSize) {
                    // There's enough content to fill the buffer
                    builder.getChars(pos, pos + bufSize, buf, 0);
                    orig.write(buf);
                    pos += bufSize;
                } else {
                    // We're near the end of the response
                    builder.getChars(pos, totalLen, buf, 0);
                    int len = totalLen - pos;
                    orig.write(buf, 0, len);
                    pos += len + 1;
                }
            }
        }

        // all state has been written. Have 'out' point to the
        // response so that all subsequent writes will make it to the
        // browser.
        out = orig;
    }

    /**
     * Get the state.
     *
     * <p>
     * In Faces it is required by the specification that the view state hidden input in each h:form has a unique id. So we
     * have to call this method multiple times as each h:form needs to generate the element id for itself.
     * </p>
     *
     * @param stateManager the state manager.
     * @param origWriter the original response writer.
     * @return the state.
     * @throws IOException when an I/O error occurs.
     */
    private void writeStateContent(StringBuilder stateBuilder) throws IOException {
        int stateLen = stateBuilder.length();
        int statePos = 0;
        while (statePos < stateLen) {
            int slen = Math.min(bufSize, stateLen - statePos);
            stateBuilder.getChars(statePos, statePos + slen, buf, 0);
            orig.write(buf, 0, slen);
            statePos += slen;
        }
    }

    /**
     * Buffers the content written after the first state marker as a list of fixed chunks: unlike a growing
     * StringBuilder it never copies what it already holds, and it is written to the response chunk by chunk. Records
     * where each state marker written in one piece starts.
     */
    static final class ChunkedWriter extends Writer {

        private static final int FIRST_CHUNK_SIZE = 1024;
        private static final int CHUNK_SIZE = 8192;
        private static final String MARKER = RIConstants.SAVESTATE_FIELD_MARKER;

        private char[][] chunks = new char[8][];
        private int chunkCount;
        private char[] current;
        private int used;
        private int completedLength;

        int[] markers = new int[2];
        int markerCount;

        ChunkedWriter() {
            current = new char[FIRST_CHUNK_SIZE];
            chunks[chunkCount++] = current;
        }

        int length() {
            return completedLength + used;
        }

        private void nextChunk() {
            completedLength += used;
            if (chunkCount == chunks.length) {
                chunks = java.util.Arrays.copyOf(chunks, chunkCount * 2);
            }
            current = new char[CHUNK_SIZE];
            chunks[chunkCount++] = current;
            used = 0;
        }

        private void recordMarker() {
            if (markerCount == markers.length) {
                markers = java.util.Arrays.copyOf(markers, markerCount * 2);
            }
            markers[markerCount++] = length();
        }

        @Override
        public void write(int c) {
            if (used == current.length) {
                nextChunk();
            }
            current[used++] = (char) c;
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            if (len == STATE_MARKER_LEN && cbuf[off] == '~' && MARKER.contentEquals(java.nio.CharBuffer.wrap(cbuf, off, len))) {
                recordMarker();
            }
            while (len > 0) {
                if (used == current.length) {
                    nextChunk();
                }
                int n = Math.min(len, current.length - used);
                System.arraycopy(cbuf, off, current, used, n);
                used += n;
                off += n;
                len -= n;
            }
        }

        @Override
        public void write(String str) {
            write(str, 0, str.length());
        }

        @Override
        public void write(String str, int off, int len) {
            if (len == STATE_MARKER_LEN && (str == MARKER && off == 0 || str.startsWith(MARKER, off))) {
                recordMarker();
            }
            while (len > 0) {
                if (used == current.length) {
                    nextChunk();
                }
                int n = Math.min(len, current.length - used);
                str.getChars(off, off + n, current, used);
                used += n;
                off += n;
                len -= n;
            }
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() {
            // no-op
        }

        /** Writes the buffered characters in [from, to) to <code>writer</code>, straight from the chunks. */
        void writeTo(Writer writer, int from, int to) throws IOException {
            int chunkStart = 0;
            for (int i = 0; i < chunkCount && chunkStart < to; i++) {
                char[] chunk = chunks[i];
                int chunkLength = i == chunkCount - 1 ? used : chunk.length;
                int chunkEnd = chunkStart + chunkLength;
                if (chunkEnd > from) {
                    int start = Math.max(from, chunkStart) - chunkStart;
                    int end = Math.min(to, chunkEnd) - chunkStart;
                    if (end > start) {
                        writer.write(chunk, start, end - start);
                    }
                }
                chunkStart = chunkEnd;
            }
        }

        StringBuilder toStringBuilder() {
            StringBuilder builder = new StringBuilder(length());
            for (int i = 0; i < chunkCount; i++) {
                builder.append(chunks[i], 0, i == chunkCount - 1 ? used : chunks[i].length);
            }
            return builder;
        }
    }

    private StringBuilder getState(StateManager stateManager, ResponseWriter origWriter) throws IOException {
        FastStringWriter stateWriter = new FastStringWriter(stateManager.isSavingStateInClient(context) ? bufSize : 128);
        context.setResponseWriter(origWriter.cloneWithWriter(stateWriter));
        if (state == null) {
            String viewId = context.getViewRoot().getViewId();

            ViewDeclarationLanguage vdl = context.getApplication().getViewHandler().getViewDeclarationLanguage(context, viewId);
            if (vdl != null) {
                Map<Object, Object> contextAttributes = context.getAttributes();
                try {
                    contextAttributes.put(IS_SAVING_STATE, TRUE);

                    state = vdl.getStateManagementStrategy(context, viewId)
                                      .saveView(context);
                } finally {
                    contextAttributes.remove(IS_SAVING_STATE);
                }
            }
        }
        stateManager.writeState(context, state);
        context.setResponseWriter(origWriter);

        return stateWriter.getBuffer();
    }

    /**
     * @param builder buffered content
     * @param offset the offset to start the search from
     * @return the index of the next delimiter, if any
     */
    private static int getNextDelimiterIndex(StringBuilder builder, int offset) {
        return builder.indexOf(RIConstants.SAVESTATE_FIELD_DELIMITER, offset);
    }

}
