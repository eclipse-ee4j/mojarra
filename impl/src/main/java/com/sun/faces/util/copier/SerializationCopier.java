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

package com.sun.faces.util.copier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Copier that copies an object by serializing and subsequently deserializing it again.
 * <p>
 * As per the platform serialization rules, the object and all its non transient dependencies have to implement the
 * {@link Serializable} interface.
 *
 * @since 2.3
 * @author Arjan Tijms
 *
 */
public class SerializationCopier implements Copier {

    private static final String SERIALIZATION_COPIER_ERROR = "SerializationCopier cannot be used in this case. Please try other copier (e.g. MultiStrategyCopier, NewInstanceCopier, CopyCtorCopier, CloneCopier).";

    @Override
    public Object copy(Object object) {

        if (!(object instanceof Serializable)) {
            throw new IllegalStateException("Can't copy object of type " + object.getClass() + " since it doesn't implement Serializable");
        }

        try {
            return copyOutIn(object);
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(SERIALIZATION_COPIER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T copyOutIn(T object) throws ClassNotFoundException, IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Out out = new Out(byteArrayOutputStream);

        out.writeObject(object);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        In in = new In(byteArrayInputStream, out);

        T cloned = (T) in.readObject();

        return cloned;
    }

    private static class In extends ObjectInputStream {

        private final Out out;

        In(InputStream inputStream, Out out) throws IOException {
            super(inputStream);
            this.out = out;
        }

        @Override
        protected Class<?> resolveProxyClass(String[] interfaceNames) throws IOException, ClassNotFoundException {
            return out.queue.poll();
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {

            String actuallyfound = null;
            Class<?> pollclass = out.queue.poll();

            if (pollclass != null) {
                actuallyfound = pollclass.getName();
            }

            if (!objectStreamClass.getName().equals(actuallyfound)) {
                throw new IllegalArgumentException(SERIALIZATION_COPIER_ERROR);
            }
            return pollclass;
        }
    }

    private static class Out extends ObjectOutputStream {

        Queue<Class<?>> queue = new LinkedList<>();

        Out(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void annotateClass(Class<?> c) {
            queue.add(c);
        }

        @Override
        protected void annotateProxyClass(Class<?> c) {
            queue.add(c);
        }
    }
}
