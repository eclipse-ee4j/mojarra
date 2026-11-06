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

package org.glassfish.mojarra.facelets.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * A property of a component is kept in its state helper under one key. {@link UIRepeat} uses the key of its superclass for an inherited property, so that both
 * classes read and write the same entry.
 */
class UIRepeatPropertyKeysTest {

    @Test
    void uiRepeatDoesNotRedeclareAnInheritedPropertyKey() {
        List<String> duplicates = new ArrayList<>();
        Map<String, Class<?>> declaringEnums = new HashMap<>();

        for (Class<?> type = UIRepeat.class; type != null; type = type.getSuperclass()) {
            for (Class<?> nested : type.getDeclaredClasses()) {
                if (!nested.isEnum() || !nested.getSimpleName().startsWith("PropertyKeys")) {
                    continue;
                }
                for (Object constant : nested.getEnumConstants()) {
                    String name = ((Enum<?>) constant).name();
                    Class<?> other = declaringEnums.putIfAbsent(name, nested);
                    if (other != null && other != nested) {
                        duplicates.add(name + " in " + other.getName() + " and " + nested.getName());
                    }
                }
            }
        }

        assertEquals(List.of(), duplicates);
    }

}
