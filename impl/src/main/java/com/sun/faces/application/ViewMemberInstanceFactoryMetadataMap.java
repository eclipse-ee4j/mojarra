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

import java.util.Map;

import com.sun.faces.util.MetadataWrapperMap;

/**
 *
 * Used to hold metadata for classes that are members of views. Does not support annotation scanning for these classes,
 * as they are not eligible for injection.
 *
 * @param <K>
 * @param <V>
 */
public class ViewMemberInstanceFactoryMetadataMap<K, V> extends MetadataWrapperMap<String, Object> {

    public ViewMemberInstanceFactoryMetadataMap(Map<String, Object> toWrap) {
        super(toWrap);
    }

    @Override
    protected Object onPut(String key, Object value) {
        return value;
    }

}
