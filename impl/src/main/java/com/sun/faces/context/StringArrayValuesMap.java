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

package com.sun.faces.context;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This is the base Map for those Maps that need to return <code>String[]</code> values.
 * <p>
 */
abstract class StringArrayValuesMap extends BaseContextMap<String[]> {

    // -------------------------------------------------------- Methods from Map

    @Override
    public boolean containsValue(Object value) {

        if (value == null || !value.getClass().isArray()) {
            return false;
        }

        Set<Map.Entry<String, String[]>> entrySet = entrySet();
        for (Map.Entry<String, String[]> entry : entrySet) {

            // values will be arrays
            if (Arrays.equals((Object[]) value, entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || !(obj.getClass() == ExternalContextImpl.theUnmodifiableMapClass)) {
            return false;
        }
        Map objMap = (Map) obj;

        if (size() != objMap.size()) {
            return false;
        }
        String[] thisKeys = keySet().toArray(new String[size()]);
        Object[] objKeys = objMap.keySet().toArray();

        Arrays.sort(thisKeys);
        Arrays.sort(objKeys);

        if (!Arrays.equals(thisKeys, objKeys)) {
            return false;
        } else {
            for (Object key : thisKeys) {
                Object[] thisVal = get(key);
                Object[] objVal = (Object[]) objMap.get(key);
                if (!Arrays.equals(thisVal, objVal)) {
                    return false;
                }
            }
        }

        return true;

    }

    @Override
    public int hashCode() {
        return this.hashCode(this);
    }

    // ------------------------------------------------------- Protected Methods

    protected int hashCode(Object someObject) {
        int hashCode = 7 * someObject.hashCode();
        for (Map.Entry<String,String[]> entry : entrySet()) {
            hashCode += entry.getKey().hashCode();
            hashCode += Arrays.hashCode(entry.getValue());
        }
        return hashCode;
    }

}
