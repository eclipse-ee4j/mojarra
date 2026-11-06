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

package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sun.faces.facelets.component.UIRepeat;

import jakarta.faces.component.html.HtmlSelectOneRadio;

/**
 * A property of a component is kept in its state helper under one key. A subclass uses the key of its superclass for
 * an inherited property, so that both classes read and write the same entry.
 */
class PropertyKeysUniquenessTest {

    /**
     * Keys which an HTML component declares again for a property of its superclass. Its enum is protected API, so a
     * subclass may use such a key, and the component keeps the property under it.
     */
    private static final Set<String> HTML_API_KEYS = Set.of(HtmlSelectOneRadio.class.getName() + "$PropertyKeys.group");

    @Test
    void noComponentRedeclaresAnInheritedPropertyKey() throws Exception {
        List<String> duplicates = new ArrayList<>();

        for (Class<?> componentClass : componentClasses()) {
            Map<String, Class<?>> declaringEnums = new HashMap<>();

            for (Class<?> type = componentClass; type != null; type = type.getSuperclass()) {
                for (Class<?> nested : type.getDeclaredClasses()) {
                    if (!nested.isEnum() || !nested.getSimpleName().startsWith("PropertyKeys")) {
                        continue;
                    }
                    for (Object constant : nested.getEnumConstants()) {
                        String name = ((Enum<?>) constant).name();
                        if (HTML_API_KEYS.contains(nested.getName() + "." + name)) {
                            continue;
                        }
                        Class<?> other = declaringEnums.putIfAbsent(name, nested);
                        if (other != null && other != nested) {
                            duplicates.add(componentClass.getSimpleName() + ": " + name + " in " + other.getName() + " and " + nested.getName());
                        }
                    }
                }
            }
        }

        assertEquals(List.of(), duplicates);
    }

    private static List<Class<?>> componentClasses() throws URISyntaxException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        addComponentClasses(UIComponent.class, classes);
        addComponentClasses(HtmlSelectOneRadio.class, classes);
        addComponentClasses(UIRepeat.class, classes);
        assertFalse(classes.isEmpty());
        return classes;
    }

    private static void addComponentClasses(Class<?> member, List<Class<?>> classes) throws URISyntaxException, ClassNotFoundException {
        File directory = new File(member.getResource(member.getSimpleName() + ".class").toURI()).getParentFile();
        String packageName = member.getPackageName();

        for (String fileName : directory.list()) {
            if (fileName.endsWith(".class") && !fileName.contains("$")) {
                Class<?> type = Class.forName(packageName + "." + fileName.substring(0, fileName.length() - ".class".length()));
                if (UIComponent.class.isAssignableFrom(type)) {
                    classes.add(type);
                }
            }
        }
    }
}
