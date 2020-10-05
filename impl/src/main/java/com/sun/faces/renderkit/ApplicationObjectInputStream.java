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

package com.sun.faces.renderkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

/**
 * An ObjectInputStream that can deserialize objects relative to the current application's class loader. In particular,
 * this class works around deserialization problems when the Faces JARs are shared (i.e. the classloader has no access to
 * application objects).
 */
public class ApplicationObjectInputStream extends ObjectInputStream {

    // Taken from ObjectInputStream to resolve primitive types
    private static final Map<String, Class<?>> PRIMITIVE_CLASSES = new HashMap<>(9, 1.0F);

    static {
        PRIMITIVE_CLASSES.put("boolean", boolean.class);
        PRIMITIVE_CLASSES.put("byte", byte.class);
        PRIMITIVE_CLASSES.put("char", char.class);
        PRIMITIVE_CLASSES.put("short", short.class);
        PRIMITIVE_CLASSES.put("int", int.class);
        PRIMITIVE_CLASSES.put("long", long.class);
        PRIMITIVE_CLASSES.put("float", float.class);
        PRIMITIVE_CLASSES.put("double", double.class);
        PRIMITIVE_CLASSES.put("void", void.class);
    }

    public ApplicationObjectInputStream() throws IOException, SecurityException {
        super();
    }

    public ApplicationObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {

        // When the container is about to call code associated with a
        // particular web application, it sets the context classloader to the
        // web app class loader. We make use of that here to locate any classes
        // that the UIComponent may hold references to. This won't cause a
        // problem to locate classes in the system class loader because
        // class loaders can look up the chain and not down the chain.
        String name = desc.getName();
        try {
            return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException cnfe) {
            Class<?> c = PRIMITIVE_CLASSES.get(name);
            if (c != null) {
                return c;
            }
            throw cnfe;
        }

    }
}
