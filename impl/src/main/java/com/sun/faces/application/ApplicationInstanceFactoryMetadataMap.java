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

package com.sun.faces.application;

import static com.sun.faces.application.ApplicationInstanceFactoryMetadataMap.METADATA.hasAnnotations;
import static com.sun.faces.util.Util.classHasAnnotations;

import java.util.HashMap;
import java.util.Map;

import com.sun.faces.util.MetadataWrapperMap;

public class ApplicationInstanceFactoryMetadataMap<K, V> extends MetadataWrapperMap<String, Object> {

    public enum METADATA {
        hasAnnotations
    }

    public ApplicationInstanceFactoryMetadataMap(Map<String, Object> toWrap) {
        super(toWrap);
    }

    public boolean hasAnnotations(String key) {
        Object objResult = getMetadata().get(key).get(hasAnnotations);

        if (objResult != null) {
            return (Boolean) objResult;
        }

        return false;
    }

    public void scanForAnnotations(String key, Class<?> value) {
        onPut(key, value);
    }

    @Override
    protected Object onPut(String key, Object value) {
        if (value instanceof Class) {
            getMetadata().computeIfAbsent(key, e -> new HashMap<>()).put(hasAnnotations, classHasAnnotations((Class<?>) value));
        }

        return null;
    }

}
