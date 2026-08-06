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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import com.sun.faces.renderkit.AttributeManager.Key;

import org.junit.jupiter.api.Test;

/**
 * Rendering decides whether to emit a component's attribute by asking the renderer's table for it by name, so an
 * attribute the table cannot find under its own name is an attribute that silently stops rendering. These tables are
 * hand-maintained literals of up to 27 entries, and the lookup is what frees them from having to be declared in any
 * particular order.
 */
class AttributesLookupTest {

    @Test
    void everyDeclaredAttributeIsFoundUnderItsOwnName() {
        for (Key key : Key.values()) {
            Attributes attributes = AttributeManager.getAttributes(key);
            assertNotNull(attributes, () -> "no table registered for " + key);

            Set<String> names = new HashSet<>();
            for (Attribute attribute : attributes) {
                assertSame(attribute, attributes.get(attribute.getName()), () -> key + " lost " + attribute.getName());
                assertTrue(names.add(attribute.getName()), () -> key + " declares " + attribute.getName() + " twice");
            }
            assertTrue(!names.isEmpty(), () -> key + " declares no attributes");
        }
    }

    @Test
    void anAttributeTheTableDoesNotDeclareIsNotFound() {
        Attributes attributes = AttributeManager.getAttributes(Key.PANELGROUP);

        assertNull(attributes.get("value"));
        assertNull(attributes.get("styleClass"));
        assertNull(attributes.get(""));
    }

    /**
     * A renderer that composes an attribute's value itself must not also declare it as a pass-through attribute, or the
     * attribute is written twice on the same element. Both button renderers compose {@code onclick} -- they append the
     * navigation script to the author's own handler -- so neither table may declare it.
     */
    @Test
    void anAttributeTheRendererComposesIsNotAlsoAPassThroughAttribute() {
        assertNull(AttributeManager.getAttributes(Key.COMMANDBUTTON).get("onclick"));
        assertNull(AttributeManager.getAttributes(Key.OUTCOMETARGETBUTTON).get("onclick"));
    }

    /**
     * Declaration order is not part of the contract, so a table whose attributes are out of alphabetical order still
     * resolves every one of them.
     */
    @Test
    void aTableDeclaredOutOfOrderStillResolves() {
        Attributes attributes = new Attributes(Attribute.attr("title"), Attribute.attr("dir"), Attribute.attr("onclick", "click"));

        assertNotNull(attributes.get("title"));
        assertNotNull(attributes.get("dir"));
        assertNotNull(attributes.get("onclick"));
        assertNull(attributes.get("lang"));
    }
}
