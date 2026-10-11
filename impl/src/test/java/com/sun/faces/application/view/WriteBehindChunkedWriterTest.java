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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.sun.faces.RIConstants;

class WriteBehindChunkedWriterTest {

    private static final String MARKER = RIConstants.SAVESTATE_FIELD_MARKER;

    @Test
    void keepsContentAndMarkerPositionsAcrossChunks() throws IOException {
        Random random = new Random(7);
        for (int round = 0; round < 200; round++) {
            WriteBehindStateWriter.ChunkedWriter writer = new WriteBehindStateWriter.ChunkedWriter();
            StringBuilder expected = new StringBuilder();
            List<Integer> markers = new ArrayList<>();
            int pieces = random.nextInt(400);
            for (int i = 0; i < pieces; i++) {
                switch (random.nextInt(5)) {
                case 0:
                    markers.add(expected.length());
                    writer.write(MARKER);
                    expected.append(MARKER);
                    break;
                case 1:
                    writer.write('~');
                    expected.append('~');
                    break;
                case 2: {
                    char[] chars = ("x".repeat(random.nextInt(9000)) + "<td>").toCharArray();
                    writer.write(chars, 0, chars.length);
                    expected.append(chars);
                    break;
                }
                case 3: {
                    // The marker handed over as a char[] slice is recognized too.
                    char[] chars = ("ab" + MARKER + "cd").toCharArray();
                    markers.add(expected.length());
                    writer.write(chars, 2, MARKER.length());
                    expected.append(MARKER);
                    break;
                }
                default: {
                    String text = "é<b>" + "y".repeat(random.nextInt(300));
                    writer.write(text, 1, text.length() - 1);
                    expected.append(text, 1, text.length());
                }
                }
            }

            assertEquals(expected.length(), writer.length());
            assertEquals(expected.toString(), writer.toStringBuilder().toString());
            assertEquals(markers.size(), writer.markerCount);
            for (int i = 0; i < markers.size(); i++) {
                assertEquals(markers.get(i), writer.markers[i]);
            }

            int from = expected.length() == 0 ? 0 : random.nextInt(expected.length());
            int to = from + (expected.length() == from ? 0 : random.nextInt(expected.length() - from + 1));
            StringWriter slice = new StringWriter();
            writer.writeTo(slice, from, to);
            assertEquals(expected.substring(from, to), slice.toString());
        }
    }

    @Test
    void splitMarkerIsNotRecorded() throws IOException {
        WriteBehindStateWriter.ChunkedWriter writer = new WriteBehindStateWriter.ChunkedWriter();
        writer.write(MARKER.substring(0, 5));
        writer.write(MARKER.substring(5));
        assertEquals(0, writer.markerCount);
        assertEquals(MARKER, writer.toStringBuilder().toString());
    }
}
