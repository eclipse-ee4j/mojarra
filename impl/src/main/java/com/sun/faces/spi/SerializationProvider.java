/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * <p>
 * This interface provides a mechanism to allow the use of alternate Java Serialization implementations.
 * </p>
 *
 * <p>
 * The implementation of this interface *must* be thread-safe and must have a no-arg constructor.
 * </p>
 */
public interface SerializationProvider {

    /**
     * <p>
     * Creates a new <code>ObjectInputStream</code> wrapping the specified <code>source</code>.
     * </p>
     *
     * <p>
     * It's <em>extremely important</em> that the ObjectInputStream returned by this method extends the serialization
     * implementation's ObjectInputStream and overrides the
     * {@link ObjectInputStream#resolveClass(java.io.ObjectStreamClass)} of to perform the following or the equivalent
     * thereof: <br>
     *
     * <pre>
     * return Class.forName(desc.getName(), true, Thread.currentThread().getContextClassLoader());
     * </pre>
     *
     * <br>
     *
     * If this step isn't done, there may be problems when deserializing.
     * </p>
     *
     * @param source the source stream from which to read the Object(s) from
     * @return an <code>ObjectInputStream</code>
     * @throws IOException if an error occurs when creating the input stream
     */
    ObjectInputStream createObjectInputStream(InputStream source) throws IOException;

    /**
     * <p>
     * Creates a new <code>ObjectOutputStream</code> wrapping the specified <code>destination</code>.
     * </p>
     *
     * @param destination the destination of the serialized Object(s)
     * @return an <code>ObjectOutputStream</code>
     * @throws IOException if an error occurs when creating the output stream
     */
    ObjectOutputStream createObjectOutputStream(OutputStream destination) throws IOException;

} // END SerializationProvider
