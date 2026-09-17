/*
 * Copyright (c) 2013, 2026 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.InvalidKeyException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

public class ByteArrayGuardAESGCMTest {

    @Test
    public void testSmallerSizeBytes() throws Exception {
        ByteArrayGuardAESGCM guard = new ByteArrayGuardAESGCM();

        // simulated flash value
        String value = "1fX_2vX";
        String encrypted = guard.encrypt(value);

        String decryptedValue = guard.decrypt(encrypted);
        assertEquals(decryptedValue, value);
    }

    @Test
    public void testDecryptValueWithoutIvBytes() throws InvalidKeyException {
        ByteArrayGuardAESGCM sut = new ByteArrayGuardAESGCM();

        String value = "noIV";
        byte[] bytes = Base64.getDecoder().decode(value);
        assertTrue(bytes.length < 12);

        assertThrows(InvalidKeyException.class, () -> sut.decrypt(value));
    }

    /**
     * A token holding a nonce but a ciphertext shorter than the authentication tag must be rejected as {@link InvalidKeyException}. The
     * JCE provider throws an unchecked exception for such a buffer on some JDKs, so {@code decrypt} guards the length before authenticating.
     */
    @Test
    public void truncatedTokenIsRejected() {
        ByteArrayGuardAESGCM guard = new ByteArrayGuardAESGCM();

        // 20 bytes = 12-byte nonce + 8 ciphertext bytes, one short of the 16-byte tag.
        String truncated = Base64.getEncoder().encodeToString(new byte[20]);

        assertThrows(InvalidKeyException.class, () -> guard.decrypt(truncated));
    }

    /**
     * GCM authenticates the token, so {@link ByteArrayGuardAESGCM#decrypt(String)} rejects a token whose ciphertext or authentication tag
     * differs from what encryption produced.
     */
    @Test
    public void modifiedTokenIsRejected() {
        ByteArrayGuardAESGCM guard = new ByteArrayGuardAESGCM();

        byte[] issued = Base64.getDecoder().decode(guard.encrypt("3Xfn_4Xfn"));

        // A byte in the ciphertext region, after the 12-byte nonce.
        byte[] modifiedCiphertext = issued.clone();
        modifiedCiphertext[12] ^= 0x01;
        assertThrows(InvalidKeyException.class, () -> guard.decrypt(Base64.getEncoder().encodeToString(modifiedCiphertext)));

        // A byte in the trailing GCM tag.
        byte[] modifiedTag = issued.clone();
        modifiedTag[modifiedTag.length - 1] ^= 0x01;
        assertThrows(InvalidKeyException.class, () -> guard.decrypt(Base64.getEncoder().encodeToString(modifiedTag)));
    }

}
