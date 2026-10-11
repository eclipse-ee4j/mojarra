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
import java.util.Arrays;
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
        fWriter.expectMarker();
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

        if (!fWriter.markerMissed && fWriter.markerCount == markersRequested) {
            // Each write following writingState() was exactly the marker, so the positions of the markers are known:
            // write the buffered chunks straight to the response around them, without searching the content. A
            // marker text which is not written right after writingState(), such as one in the page's own text, is not
            // a marker and is written as is.
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

        // A write following writingState() was not exactly the marker (e.g. a buffering ResponseWriter wrapper split
        // it): search the content for every marker text.
        StringBuilder builder = fWriter.toStringBuilder();

        // Begin writing...
        int totalLen = builder.length();
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
                        writeStateContent(stateBuilder);
                        // Push us past the last '~' at the end of the marker
                        pos += len + STATE_MARKER_LEN;
                        tildeIdx = getNextDelimiterIndex(builder, pos);

                        stateBuilder = getState(stateManager, origWriter);
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
     * Writes the state to the response, through the buffer.
     *
     * @param stateBuilder the state, as {@link #getState} returned it.
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
     * Buffers the content written after the first state marker as a list of fixed chunks, which it never copies again,
     * and writes them to the response chunk by chunk. After {@link #expectMarker()}, records where the next write
     * containing a <code>~</code> starts when it is exactly the state marker, and otherwise that a marker was missed.
     * Writes without a <code>~</code> before it, such as the end of a start tag, cannot hold a part of the marker.
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
        boolean markerMissed;
        private boolean expectingMarker;

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
                chunks = Arrays.copyOf(chunks, chunkCount * 2);
            }
            current = new char[CHUNK_SIZE];
            chunks[chunkCount++] = current;
            used = 0;
        }

        /** The next write is expected to be the state marker. */
        void expectMarker() {
            if (expectingMarker) {
                markerMissed = true;
            }
            expectingMarker = true;
        }

        /**
         * Called before a write containing a <code>~</code> while a marker is expected: records the marker when the
         * write is exactly the marker.
         */
        private void markerWritten(boolean isMarker) {
            expectingMarker = false;
            if (!isMarker) {
                markerMissed = true;
                return;
            }
            if (markerCount == markers.length) {
                markers = Arrays.copyOf(markers, markerCount * 2);
            }
            markers[markerCount++] = length();
        }

        private void checkMarker(char[] cbuf, int off, int len) {
            for (int i = 0; i < len; i++) {
                if (cbuf[off + i] == '~') {
                    markerWritten(i == 0 && isMarker(cbuf, off, len));
                    return;
                }
            }
        }

        private static boolean isMarker(char[] cbuf, int off, int len) {
            if (len != STATE_MARKER_LEN) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if (cbuf[off + i] != MARKER.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        private void checkMarker(String str, int off, int len) {
            int tilde = str.indexOf('~', off);
            if (tilde >= 0 && tilde < off + len) {
                markerWritten(len == STATE_MARKER_LEN && tilde == off && (str == MARKER && off == 0 || str.startsWith(MARKER, off)));
            }
        }

        @Override
        public void write(int c) {
            if (expectingMarker && c == '~') {
                markerWritten(false);
            }
            if (used == current.length) {
                nextChunk();
            }
            current[used++] = (char) c;
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            if (expectingMarker) {
                checkMarker(cbuf, off, len);
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
            if (expectingMarker) {
                checkMarker(str, off, len);
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
