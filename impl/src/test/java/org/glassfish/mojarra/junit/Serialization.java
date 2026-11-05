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

package org.glassfish.mojarra.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Java serialization round trips for tests.
 */
public final class Serialization {

    private Serialization() {
    }

    /**
     * @param <T> the type of the object
     * @param object the object to serialize
     * @return a deserialized copy of the given object
     * @throws IOException if serializing or deserializing fails
     * @throws ClassNotFoundException if a serialized class cannot be loaded
     */
    @SuppressWarnings("unchecked")
    public static <T> T serializeAndDeserialize(T object) throws IOException, ClassNotFoundException {
        return (T) deserialize(serialize(object));
    }

    /**
     * @param object the object to serialize
     * @return the serialized bytes of the given object
     * @throws IOException if serializing fails
     */
    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(object);
        }
        return bytes.toByteArray();
    }

    /**
     * @param serialized the serialized bytes of an object
     * @return the deserialized object
     * @throws IOException if deserializing fails
     * @throws ClassNotFoundException if a serialized class cannot be loaded
     */
    public static Object deserialize(byte[] serialized) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serialized))) {
            return in.readObject();
        }
    }

    /**
     * Sets the serialVersionUID of the given class in the given serialized bytes, as if the class was serialized with that serialVersionUID. It follows the
     * class name, which the class descriptor writes with its length in front, and which an array class descriptor holds inside a longer name.
     *
     * @param serialized the serialized bytes holding a class descriptor of the given class
     * @param className the binary name of the class
     * @param serialVersionUID the serialVersionUID to set
     * @return the given bytes, changed
     */
    public static byte[] withSerialVersionUID(byte[] serialized, String className, long serialVersionUID) {
        byte[] name = className.getBytes(StandardCharsets.UTF_8);
        byte[] lengthAndName = ByteBuffer.allocate(Short.BYTES + name.length).putShort((short) name.length).put(name).array();
        for (int i = 0; i + lengthAndName.length <= serialized.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(serialized, i, i + lengthAndName.length), lengthAndName)) {
                ByteBuffer.wrap(serialized, i + lengthAndName.length, Long.BYTES).putLong(serialVersionUID);
                return serialized;
            }
        }
        throw new IllegalArgumentException("No class descriptor of " + className);
    }

}
