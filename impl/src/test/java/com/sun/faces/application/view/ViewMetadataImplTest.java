/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package com.sun.faces.application.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ViewMetadataImplTest {

    public interface Labeled {
        String LABEL = "labeled";
        String SHARED = "labeled";
    }

    public enum Weekday {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    }

    public enum LabeledWeekday implements Labeled {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    }

    public static class Parent {
        public static final String SHARED = "parent";
        public static final String PARENT_ONLY = "parent";
    }

    public static class Child extends Parent implements Labeled {
        public static final String SHARED = "child";
        public static final String CHILD_ONLY = "child";
    }

    /**
     * Enum constants are exposed in the order in which they are declared, so that <code>#{Weekday.values()}</code>
     * feeds <code>f:selectItems</code> the same order as <code>Weekday.values()</code> does.
     */
    @Test
    public void collectConstantsKeepsDeclarationOrderOfEnumConstants() {
        Map<String, Object> constants = ViewMetadataImpl.collectConstants(Weekday.class.getName());

        assertEquals(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"), List.copyOf(constants.keySet()));
    }

    /**
     * The constants of the type itself come before the ones it inherits, so that the constants an enum inherits from
     * an interface trail its own members instead of being mixed in between them.
     */
    @Test
    public void collectConstantsKeepsDeclarationOrderOfEnumConstantsBeforeInheritedOnes() {
        Map<String, Object> constants = ViewMetadataImpl.collectConstants(LabeledWeekday.class.getName());

        assertEquals(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY", "LABEL", "SHARED"),
                List.copyOf(constants.keySet()));
    }

    /**
     * Constants inherited from super classes and from interfaces are collected as well, the super classes first.
     */
    @Test
    public void collectConstantsIncludesInheritedClassAndInterfaceConstants() {
        Map<String, Object> constants = ViewMetadataImpl.collectConstants(Child.class.getName());

        assertEquals(List.of("SHARED", "CHILD_ONLY", "PARENT_ONLY", "LABEL"), List.copyOf(constants.keySet()));
    }

    /**
     * When a constant field name is declared more than once in the hierarchy, then the declaration of the most
     * specific type is the one exposed.
     */
    @Test
    public void collectConstantsPrefersMostSpecificDeclarationOfSharedConstant() {
        assertEquals("child", ViewMetadataImpl.collectConstants(Child.class.getName()).get("SHARED"));
        assertEquals("parent", ViewMetadataImpl.collectConstants(Parent.class.getName()).get("SHARED"));
        assertEquals("labeled", ViewMetadataImpl.collectConstants(Labeled.class.getName()).get("SHARED"));
    }

    /**
     * A constant which does not exist at all is a typo in the view, which must not silently evaluate to empty.
     */
    @Test
    public void collectConstantsThrowsIllegalArgumentExceptionOnUnknownConstant() {
        Map<String, Object> constants = ViewMetadataImpl.collectConstants(Weekday.class.getName());

        assertThrows(IllegalArgumentException.class, () -> constants.get("CANEDAY"));
    }

}
