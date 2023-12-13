/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sun.faces.lifecycle;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lu4242
 */
public class TokenGenerator {
    private final AtomicLong seed;

    public TokenGenerator() {
        seed = new AtomicLong(generateSeed());
    }

    /**
     * Get the next token to be assigned to this request
     *
     * @return
     */
    public String getNextToken() {
        // atomically increment the value
        long nextToken = seed.incrementAndGet();

        // convert using base 36 because it is a fast efficient subset of base-64
        return Long.toString(nextToken, 36);
    }

    private long generateSeed() {
        SecureRandom rng;
        try {
            rng = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            // "Every implementation of the Java platform is required to support at least one strong SecureRandom
            // implementation" but the method can still throw this Exception, so we fall back to this just in case.
            rng = new SecureRandom();
        }

        // use 48 bits for strength and fill them in
        byte[] randomBytes = new byte[6];
        rng.nextBytes(randomBytes);

        // convert to a long
        return new BigInteger(randomBytes).longValue();
    }
}
