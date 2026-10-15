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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.ConcurrentCalls;
import com.sun.faces.junit.LogRecords;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

class ByteArrayGuardTest {

    private FacesContext context;

    @BeforeEach
    void setUp() {
        context = mock(FacesContext.class);
        when(context.getExternalContext()).thenReturn(mock(ExternalContext.class));
    }

    @Test
    void roundTrip() {
        ByteArrayGuard guard = new ByteArrayGuard();
        byte[] state = "view state".getBytes(UTF_8);

        assertArrayEquals(state, guard.decrypt(context, guard.encrypt(context, state)));
    }

    /**
     * A state whose encrypted data differs from what encryption produced fails the MAC check, and must not leave the guard unable to
     * decrypt valid states afterwards.
     */
    @Test
    void validStateIsAcceptedAfterRejectedOne() {
        ByteArrayGuard guard = new ByteArrayGuard();
        byte[] state = "view state".getBytes(UTF_8);
        byte[] issued = guard.encrypt(context, state);
        byte[] modified = issued.clone();
        modified[modified.length - 1] ^= 0x01;

        for (int i = 0; i < 3; i++) {
            assertNull(guard.decrypt(context, modified));
            assertArrayEquals(state, guard.decrypt(context, issued));
        }
    }

    /**
     * A state encrypted with another key, e.g. by another server or before a restart, fails the MAC check. This is otherwise only visible as a
     * ViewExpiredException, so the guard must log a warning which is visible by default, names the "MAC did not verify" symptom, and points to the
     * faces/ClientSideSecretKey env-entry.
     */
    @Test
    void stateEncryptedWithOtherKeyLogsWarningPointingToSecretKey() {
        byte[] issued = new ByteArrayGuard().encrypt(context, "view state".getBytes(UTF_8));
        ByteArrayGuard otherServer = new ByteArrayGuard();

        List<LogRecord> warnings = LogRecords.capture(FacesLogger.RENDERKIT.getLogger(), Level.WARNING, () -> assertNull(otherServer.decrypt(context, issued)));

        assertEquals(1, warnings.size(), "one warning per rejected state");
        String message = warnings.get(0).getMessage();
        assertTrue(message.startsWith("MAC did not verify!"), message);
        assertTrue(message.contains("faces/ClientSideSecretKey"), message);
    }

    /**
     * One guard serves all requests of the application, so concurrent calls must not interfere.
     */
    @Test
    void concurrentCallsRoundTrip() throws Exception {
        ByteArrayGuard guard = new ByteArrayGuard();

        ConcurrentCalls.run(id -> {
            byte[] state = id.getBytes(UTF_8);
            assertArrayEquals(state, guard.decrypt(context, guard.encrypt(context, state)));
        });
    }

}
