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

package org.glassfish.mojarra.renderkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.mojarra.spi.SerializationProvider;

/**
 * Delegates to another {@link SerializationProvider}, and records every source an {@link ObjectInputStream} is created for.
 */
class RecordingSerializationProvider implements SerializationProvider {

    private final SerializationProvider delegate;
    private final List<InputStream> sources = new ArrayList<>();

    RecordingSerializationProvider(SerializationProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public ObjectInputStream createObjectInputStream(InputStream source) throws IOException {
        sources.add(source);
        return delegate.createObjectInputStream(source);
    }

    @Override
    public ObjectOutputStream createObjectOutputStream(OutputStream destination) throws IOException {
        return delegate.createObjectOutputStream(destination);
    }

    List<InputStream> getSources() {
        return List.copyOf(sources);
    }

}
