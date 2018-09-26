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

package com.sun.faces.util;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.faces.FacesException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * <p>This utility class is to provide both encryption and
 * decryption <code>Ciphers</code> to <code>ResponseStateManager</code>
 * implementations wishing to provide encryption support.</p>
 * 
 * <p>The algorithm used to encrypt byte array is AES with CBC.</p>
 *  
 * <p>Original author Inderjeet Singh, J2EE Blue Prints Team. Modified to suit JSF
 * needs.</p> 
 */
public final class ByteArrayGuardAESCTR {


     // Log instance for this class
    private static final Logger LOGGER = FacesLogger.RENDERKIT.getLogger();

    private static final int KEY_LENGTH = 128;
    private static final int IV_LENGTH = 16;

    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_CODE = "AES/CTR/NoPadding";
    
    private SecretKey sk;
    
    private IvParameterSpec ivspec;
    
    private Charset utf8;

    // ------------------------------------------------------------ Constructors

    public ByteArrayGuardAESCTR() {

        try {
            setupKeyAndCharset();
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.SEVERE)) { 
                LOGGER.log(Level.SEVERE,
                           "Unexpected exception initializing encryption."
                           + "  No encryption will be performed.",
                           e);
            }
            System.err.println("ERROR: Initializing Ciphers");
        }
    }

    // ---------------------------------------------------------- Public Methods    


    /**
     * This method:
     *    Encrypts bytes using a cipher.  
     *    Generates MAC for intialization vector of the cipher
     *    Generates MAC for encrypted data
     *    Returns a byte array consisting of the following concatenated together:
     *       |MAC for cnrypted Data | MAC for Init Vector | Encrypted Data |
     * @param bytes The byte array to be encrypted.
     * @return the encrypted byte array.
     */
    public String encrypt(String value) {
        String securedata = null;
        byte[] bytes = value.getBytes(utf8);
        try {
            Cipher encryptCipher = Cipher.getInstance(CIPHER_CODE);
            encryptCipher.init(Cipher.ENCRYPT_MODE, sk, ivspec);
            // encrypt the plaintext
            byte[] encdata = encryptCipher.doFinal(bytes);
            // Base64 encode the encrypted bytes
            securedata = Base64.getEncoder().encodeToString(encdata);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE,
                           "Unexpected exception initializing encryption."
                           + "  No encryption will be performed.",
                           e);
            }
            return null;
        }
        return securedata;
    }

    public String decrypt(String value) throws InvalidKeyException {
        
        byte[] bytes = Base64.getDecoder().decode(value);
        
        try {
            Cipher decryptCipher = Cipher.getInstance(CIPHER_CODE);
            decryptCipher.init(Cipher.DECRYPT_MODE, sk, ivspec);

            byte[] plaindata = decryptCipher.doFinal(bytes);
            for (byte cur : plaindata) {
                // Values < 0 cause the conversion to text to fail.
                if (cur < 0 || cur > Byte.MAX_VALUE) {
                    throw new InvalidKeyException("Invalid characters in decrypted value");
                }
            }
            return new String(plaindata, utf8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException nsae) {
            throw new InvalidKeyException(nsae);
        }
    }
    
    // --------------------------------------------------------- Private Methods

    private void setupKeyAndCharset() {

        try {
            InitialContext context = new InitialContext();
            String encodedKeyArray = (String) context.lookup("java:comp/env/jsf/FlashSecretKey");
            if (null != encodedKeyArray) {
                byte[] keyArray = Base64.getDecoder().decode(encodedKeyArray);
                if (keyArray.length < 16) {
                    throw new FacesException("key must be at least 16 bytes long.");
                }
                sk = new SecretKeySpec(keyArray, KEY_ALGORITHM);
                byte[] iv = new byte[16];
                System.arraycopy(keyArray, 0, iv, 0, 16);
                ivspec = new IvParameterSpec(iv);
            }
        } catch(NamingException exception) {
            if (LOGGER.isLoggable(Level.FINEST)) { 
                LOGGER.log(Level.FINEST, "Unable to find the encoded key.", exception);
            }
        } catch (FacesException e) {
            throw new FacesException(e);
        }
        
        if (null == sk) {
            try {
                KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
                kg.init(KEY_LENGTH);   // 256 if you're using the Unlimited Policy Files
                sk = kg.generateKey(); 
                SecureRandom rand = new SecureRandom();
                byte[] iv = new byte[16];
                rand.nextBytes(iv);
                ivspec = new IvParameterSpec(iv);
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        
            
        SortedMap<String,Charset> availableCharsets = Charset.availableCharsets();
        if (availableCharsets.containsKey("UTF-8")) {
            utf8 = availableCharsets.get("UTF-8");
        } else if (availableCharsets.containsKey("UTF8")) {
            utf8 = availableCharsets.get("UTF8");
        } else {
            throw new FacesException("Unable to get UTF-8 Charset.");
        }
        
    }

}
