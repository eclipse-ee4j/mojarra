/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.faces.renderkit;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.CompressViewState;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.junit.JUnitFacesTestCaseBase;

import jakarta.faces.application.StateManager;

class ServerSideStateHelperReadTest extends JUnitFacesTestCaseBase {

    private static final String STATE = "state";

    /**
     * A serialized and compressed server side state is read through a {@link BufferedInputStream}, because {@link ObjectInputStream} reads its headers a
     * byte at a time and {@link GZIPInputStream} inflates on every read.
     */
    @Test
    void serializedCompressedStateIsReadThroughABuffer() throws Exception {
        servletContext.addInitParameter(StateManager.SERIALIZE_SERVER_STATE_PARAM_NAME, "true");
        servletContext.addInitParameter(CompressViewState.getQualifiedName(), "true");
        servletContext.removeAttribute(WebConfiguration.class.getName());
        ServerSideStateHelper helper = new ServerSideStateHelper();
        RecordingSerializationProvider provider = new RecordingSerializationProvider(helper.serialProvider);
        helper.serialProvider = provider;

        assertEquals(STATE, helper.handleRestoreState(compressed(STATE)));
        assertEquals(1, provider.getSources().size(), "object input streams created");
        assertInstanceOf(BufferedInputStream.class, provider.getSources().get(0));
    }

    private static byte[] compressed(Object state) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(bytes))) {
            out.writeObject(state);
        }
        return bytes.toByteArray();
    }

}
