/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * One renderer's pass-through attribute table, which can be asked for an attribute by name or iterated whole.
 * <p>
 * The by-name index is what lets the table be declared in any order: a lookup that searched the attributes instead
 * would need them sorted by name, an invariant that cannot be checked at the declaration site and that fails silently
 * -- an attribute declared out of order would simply stop rendering.
 *
 * @see AttributeManager#getAttributes(AttributeManager.Key)
 */
public final class Attributes implements Iterable<Attribute> {

    private final Attribute[] attributes;

    private final Map<String, Attribute> byName;

    Attributes(Attribute... attributes) {
        this.attributes = attributes;
        byName = new HashMap<>(attributes.length, 1.0f);
        for (Attribute attribute : attributes) {
            byName.put(attribute.getName(), attribute);
        }
    }

    /**
     * Returns the attribute this table holds under the given name, or {@code null} when it holds none.
     *
     * @param name the attribute name to look up
     * @return the named attribute, or {@code null}
     */
    public Attribute get(String name) {
        return byName.get(name);
    }

    @Override
    public Iterator<Attribute> iterator() {
        return Arrays.asList(attributes).iterator();
    }
}
