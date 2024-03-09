/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.DatatypeConverter;


public class ByteArrayGuardAESCTRTest {

    @Test
    public void testSmallerSizeBytes() throws Exception {
        ByteArrayGuardAESCTR guard = new ByteArrayGuardAESCTR();

	// simulated flash value
        String value = "1fX_2vX";
        String encrypted = guard.encrypt(value);
        assertTrue(encrypted.length() < 33);

        String decryptedValue = guard.decrypt(encrypted);
        assertEquals(decryptedValue, value);


    }

    @Test
    public void testDecryptValueWithoutIvBytes() throws InvalidKeyException {
        ByteArrayGuardAESCTR sut = new ByteArrayGuardAESCTR();

        String value = "noIV";
        byte[] bytes = DatatypeConverter.parseBase64Binary(value);
        assertTrue(bytes.length < 16);

        assertThrows(InvalidKeyException.class, () -> sut.decrypt(value));
    }

}

