/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2005-2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.faces.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/*
 * From: http://blog.crazybob.org/2007/02/debugging-serialization.html
 *
 * Usage example:

                DebugObjectOutputStream out =
                        new DebugObjectOutputStream(oos);
                try {
                    out.writeObject(stateToWrite[1]);
                } catch (Exception e) {
                    DebugUtil.printState((Map)stateToWrite[1], LOGGER);
                    throw new FacesException(
                            "Serialization error. Path to offending instance: "
                            + out.getStack(), e);
                }



 *
 *
 */

public class DebugObjectOutputStream extends ObjectOutputStream {

    private static final Field DEPTH_FIELD;
    static {
        try {
            DEPTH_FIELD = ObjectOutputStream.class.getDeclaredField("depth");
            DEPTH_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    final List<Object> stack = new ArrayList<>();

    /**
     * Indicates whether or not OOS has tried to write an IOException (presumably as the result of a serialization error) to
     * the stream.
     */
    boolean broken = false;

    public DebugObjectOutputStream(OutputStream out) throws IOException {
        super(out);
        enableReplaceObject(true);
    }

    /**
     * Abuse {@code replaceObject()} as a hook to maintain our stack.
     */
    @Override
    protected Object replaceObject(Object o) {
        // ObjectOutputStream writes serialization
        // exceptions to the stream. Ignore
        // everything after that so we don't lose
        // the path to a non-serializable object. So
        // long as the user doesn't write an
        // IOException as the root object, we're OK.
        int currentDepth = currentDepth();
        if (o instanceof IOException && currentDepth == 0) {
            broken = true;
        }
        if (!broken) {
            truncate(currentDepth);
            stack.add(o);
        }
        return o;
    }

    private void truncate(int depth) {
        while (stack.size() > depth) {
            pop();
        }
    }

    private Object pop() {
        return stack.remove(stack.size() - 1);
    }

    /**
     * Returns a 0-based depth within the object graph of the current object being serialized.
     */
    private int currentDepth() {
        try {
            Integer oneBased = (Integer) DEPTH_FIELD.get(this);
            return oneBased - 1;
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the path to the last object serialized. If an exception occurred, this should be the path to the
     * non-serializable object.
     */
    public List<Object> getStack() {
        return stack;
    }
}
