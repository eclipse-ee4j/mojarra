/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.RIConstants;

import jakarta.faces.application.Application;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * {@link WriteBehindStateWriter#flushToWriter()}: where the markers are known (each write following writingState() is
 * exactly the marker), and where the buffered content is searched for them, the response is the same.
 */
class WriteBehindStateWriterFlushTest {

    private static final String MARKER = RIConstants.SAVESTATE_FIELD_MARKER;
    private static final int BUFFER_SIZE = 64;

    private FacesContext context;
    private final AtomicInteger states = new AtomicInteger();

    @BeforeEach
    void setUp() throws IOException {
        context = mock(FacesContext.class);
        AtomicReference<Writer> stateWriter = new AtomicReference<>();

        ResponseWriter responseWriter = mock(ResponseWriter.class);
        when(responseWriter.cloneWithWriter(any())).thenAnswer(invocation -> {
            stateWriter.set(invocation.getArgument(0));
            return mock(ResponseWriter.class);
        });
        when(context.getResponseWriter()).thenReturn(responseWriter);
        when(context.getAttributes()).thenReturn(new HashMap<>());

        StateManager stateManager = mock(StateManager.class);
        doAnswer(invocation -> {
            // A state long enough to go through the buffer in several writes.
            stateWriter.get().write("[STATE" + states.incrementAndGet() + "|" + "s".repeat(150) + "]");
            return null;
        }).when(stateManager).writeState(eq(context), any());

        Application application = mock(Application.class);
        when(context.getApplication()).thenReturn(application);
        when(application.getStateManager()).thenReturn(stateManager);
        when(application.getViewHandler()).thenReturn(mock(ViewHandler.class));
        UIViewRoot viewRoot = mock(UIViewRoot.class);
        when(viewRoot.getViewId()).thenReturn("/page.xhtml");
        when(context.getViewRoot()).thenReturn(viewRoot);
    }

    private static String state(int number) {
        return "[STATE" + number + "|" + "s".repeat(150) + "]";
    }

    private interface Page {
        void render(WriteBehindStateWriter writer) throws IOException;
    }

    private String render(Page page) throws IOException {
        states.set(0);
        StringWriter response = new StringWriter();
        WriteBehindStateWriter writer = new WriteBehindStateWriter(response, context, BUFFER_SIZE);
        try {
            page.render(writer);
            writer.flushToWriter();
            writer.write("<!-- after -->");
        } finally {
            writer.release();
        }
        return response.toString();
    }

    private static void form(WriteBehindStateWriter writer, String id, boolean splitMarker) throws IOException {
        writer.write("<form id=\"" + id + "\">~<input/>");
        writer.writingState();
        if (splitMarker) {
            writer.write(MARKER.substring(0, 7));
            writer.write(MARKER.substring(7).toCharArray());
        } else {
            writer.write(MARKER);
        }
        writer.write("</form>\n" + "<p>text ~ with tildes~</p>".repeat(40));
    }

    @Test
    void severalFormsGetTheSameResponseWithKnownAndSearchedMarkers() throws IOException {
        String expected = "<html>" + "<form id=\"a\">~<input/>" + state(1) + "</form>\n" + "<p>text ~ with tildes~</p>".repeat(40)
                + "<form id=\"b\">~<input/>" + state(2) + "</form>\n" + "<p>text ~ with tildes~</p>".repeat(40)
                + "<form id=\"c\">~<input/>" + state(3) + "</form>\n" + "<p>text ~ with tildes~</p>".repeat(40) + "</html><!-- after -->";

        for (int split = -1; split < 3; split++) {
            int splitForm = split;
            String response = render(writer -> {
                writer.write("<html>");
                form(writer, "a", splitForm == 0);
                form(writer, "b", splitForm == 1);
                form(writer, "c", splitForm == 2);
                writer.write("</html>");
            });
            assertEquals(expected, response, "split marker in form " + split);
        }
    }

    @Test
    void markerTextInThePageIsReplacedWhenAMarkerIsSplit() throws IOException {
        // As before: when the content is searched, every marker text gets a state.
        String response = render(writer -> {
            form(writer, "a", true);
            writer.write("<p>" + MARKER + "</p>");
        });
        assertEquals("<form id=\"a\">~<input/>" + state(1) + "</form>\n" + "<p>text ~ with tildes~</p>".repeat(40) + "<p>" + state(2)
                + "</p><!-- after -->", response);
    }

    @Test
    void markerTextInThePageIsKeptWhenTheMarkersAreKnown() throws IOException {
        String response = render(writer -> {
            form(writer, "a", false);
            writer.write("<p>user: " + MARKER + "</p>");
            writer.write("<p>" + MARKER + "</p>");
        });
        assertEquals("<form id=\"a\">~<input/>" + state(1) + "</form>\n" + "<p>text ~ with tildes~</p>".repeat(40) + "<p>user: " + MARKER
                + "</p><p>" + MARKER + "</p><!-- after -->", response);
    }

    @Test
    void markerWrittenByOneCharacterIsSearched() throws IOException {
        String response = render(writer -> {
            writer.write("<form>");
            writer.writingState();
            for (char c : MARKER.toCharArray()) {
                writer.write(c);
            }
            writer.write("</form>");
        });
        assertEquals("<form>" + state(1) + "</form><!-- after -->", response);
    }
}
